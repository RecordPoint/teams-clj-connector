(ns teams-connector-clj.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [expectations.clojure.test :refer [defexpect expect expecting side-effects]]
            [teams-connector-clj.core :refer [app]]
            [ring.mock.request :as mock]
            [camel-snake-kebab.core :as csk]
            [cheshire.core :as json]
            [teams-connector-clj.graph-api :as graph]
            [teams-connector-clj.r365      :as r365]
            [teams-connector-clj.api       :as api]
            [imminent.executors :as executors]
            [imminent.core :as immi]))


(def message-created-event {"changeType" "created",
                            "tenantId" "b8175554-0945-4fd2-b5c9-840f12c7ae56",
                            "resource"
                            "teams('123')/channels('456')/messages('789')"})


(defexpect app-test
  (expecting "Graph API Validation Webhook"
             (expecting "Succeeds when any validation token is given"
                        (let [validation-token "SAMPLE_TOKEN"
                              uri              (format "/api/graph/subscriptions/notifications?validationToken=%s" validation-token)]
                          (expect (app (mock/request :post uri))
                                  {:status  200
                                   :headers {"content-type" "text/plain"}
                                   :body    validation-token})))

             (expecting "Fails when no validation token or payload is given"
                        (expect {:status  400
                                 :headers {"content-type" "text/plain"}
                                 :body    "Bad request"}
                                (app (mock/request :post "/api/graph/subscriptions/notifications")))))


  (expecting "Webhook notification processing"
             (binding [imminent.executors/*executor* imminent.executors/blocking-executor]
               (expecting "Triggers background subscription handling and acknowledges notification receipt"
                          (let [payload {"value" [{"changeType" "created",
                                                   "tenantId" "b8175554-0945-4fd2-b5c9-840f12c7ae56",
                                                   "resource"
                                                   "teams('123')/channels('456')/messages('789')"}]}
                                event   (-> (payload "value") first)]
                            (expect  [nil
                                      [event :token]]
                                     (side-effects [[graph/request-token              (immi/const-future :token)]
                                                    [api/process-subscription-event   (immi/const-future :ok)]]
                                                   (let [request  (-> (mock/request
                                                                       :post "/api/graph/subscriptions/notifications")
                                                                      (mock/json-body payload))
                                                         response (app request)]

                                                     (expect 202        (:status response))
                                                     (expect "Accepted" (:body response)))))))



               (expecting "Processes subscription event"
                          (let [mock-team    {:id           "mock-team"
                                              :display-name "Star Wars"}
                                mock-channel {:id           "mock-channel"
                                              :display-name "A new hope"}
                                mock-message {:id           "mock-message"
                                              :body         {:content      "hello"}
                                              :from         {:user {:display-name "Leo"}}
                                              :etag         "version"
                                              :last-modified-date-time "2020-01-10T01:25:16.790Z"
                                              :created-date-time       "2020-01-10T01:25:16.790Z"}
                                mock-replies [{:id         "mock-reply"
                                               :content    "oh, hai"}]

                                expected-message (assoc mock-message :replies mock-replies)]

                            (expect [["123" "456" "789" "token"]
                                     ["123" "456" "789" "token"]
                                     ["123" "token"]
                                     ["123" "456" "token"]
                                     nil
                                     [mock-team mock-channel expected-message :rtoken]  ; submit
                                     ]
                                    (side-effects [[graph/message      (immi/const-future mock-message)]
                                                   [graph/replies      (immi/const-future mock-replies)]
                                                   [graph/team         (immi/const-future mock-team)]
                                                   [graph/channel      (immi/const-future mock-channel)]
                                                   [r365/request-token (immi/const-future :rtoken)]
                                                   [api/submit-record  (immi/const-future :ok)]]
                                                  (expect :ok
                                                          (immi/dderef (api/process-subscription-event
                                                                        message-created-event
                                                                        "token")))))))))


  (binding [imminent.executors/*executor* imminent.executors/blocking-executor]
    (expecting "API starts monitoring and ingesting channel"
               (let [mock-team           {:id           "123"
                                          :display-name "Star Wars"}
                     mock-channel        {:id           "456"
                                          :display-name "A new hope"}
                     mock-messages       [{:id           "789"
                                           :body         {:content      "hello"}
                                           :from         {:user {:display-name "Leo"}}
                                           :etag         "version"
                                           :last-modified-date-time "2020-01-10T01:25:16.790Z"
                                           :created-date-time       "2020-01-10T01:25:16.790Z"}]
                     mock-replies        [{:id         "mock-reply"
                                           :content    "oh, hai"}]
                     expected-message     (assoc (first mock-messages) :replies mock-replies)
                     expected-binary-meta {:connector-id "50ab69f4-32aa-4221-94ac-1c186f0f8102",
                                           :item-external-id "messageid-789",
                                           :binary-external-id "chatjson-messageid-789"}
                     expected-record      {:source-last-modified-by "Leo",
                                           :content-version "version",
                                           :mime-type "text/plain",
                                           :parent-external-id "teams%3A123%3Achannels%3A456",
                                           :source-last-modified-date "2020-01-10T01:25:16.790Z",
                                           :title "hello...",
                                           :author "Leo",
                                           :source-created-by "Leo",
                                           :source-created-date "2020-01-10T01:25:16.790Z",
                                           :external-id "messageid-789",
                                           :connector-id "50ab69f4-32aa-4221-94ac-1c186f0f8102",
                                           :location "Star Wars > A new hope",
                                           :media-type "Electronic"}]
                 (expect [nil                        ; graph-token
                          nil                        ; r-token
                          ["123" "456" :token]       ; subscribe
                          ["123" "456" :token]       ; messages
                          ["123" :token]             ; team
                          ["123" "456" :token]       ; channel
                          ["123" "456" "789" :token] ; replies
                          [expected-record :rtoken]  ; submit
                          [expected-binary-meta expected-message :rtoken]  ; binary
                          ]
                         (side-effects
                          [[graph/request-token        (immi/const-future :token)]
                           [r365/request-token         (immi/const-future :rtoken)]
                           [graph/subscribe-to-channel (immi/const-future :subscribed)]
                           [graph/messages             (immi/const-future mock-messages)]
                           [graph/team                 (immi/const-future mock-team)]
                           [graph/channel              (immi/const-future mock-channel)]
                           [graph/replies              (immi/const-future mock-replies)]
                           [r365/submit-record         (immi/const-future :ok)]
                           [r365/submit-binary         (immi/const-future :ok)]
                           ]
                          (expect [:ok]
                                  (immi/dderef (api/ingest-channel "123" "456")))))))))
