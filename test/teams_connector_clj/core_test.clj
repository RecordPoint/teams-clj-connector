(ns teams-connector-clj.core-test
  (:require [clojure.test :refer :all]
            [teams-connector-clj.core :refer :all]
            [ring.mock.request :as mock]
            [camel-snake-kebab.core :as csk]
            [cheshire.core :as json]))

(deftest app-test
  (testing "Graph API Validation Webhook"
    (testing "Succeeds when any validation token is given"
      (let [validation-token "SAMPLE_TOKEN"
            uri              (format "/api/graph/subscriptions/notifications?validationToken=%s" validation-token)]
        (is (= (app (mock/request :post uri))
               {:status  200
                :headers {"content-type" "text/plain"}
                :body    validation-token}))))

    (testing "Fails when no validation token or payload is given"
      (is (= {:status  400
              :headers {"content-type" "text/plain"}
              :body    "Bad request"}
             (app (mock/request :post "/api/graph/subscriptions/notifications"))))))


  (testing "Webhook notification processing"
    (testing "Acknowledges notification receipt"
      (let [payload {"value" [{"subscriptionId"                 "ba1bd098-356c-49f6-a477-36f665522458"
                               "changeType"                     "created"
                               "tenantId"                       "b8175554-0945-4fd2-b5c9-840f12c7ae56"
                               "clientState"                    "optional"
                               "subscriptionExpirationDateTime" "2019-12-30T07:16:44.69+00:00"
                               "resource"                       "teams('t-id')/channels('c-id')/messages('m-id')"
                               "resourceData"
                               {"@odata.type" "#Microsoft.Graph.ChatMessage"
                                "@odata.id" "teams('t-id')/channels('c-id')/messages('m-id')"}
                               "encryptedContent" nil}]}
            request  (-> (mock/request :post "/api/graph/subscriptions/notifications")
                         (mock/json-body payload))
            response (app request)]

        (are [x y] (= x y)  
          202        (:status response)
          "Accepted" (:body response))))))
