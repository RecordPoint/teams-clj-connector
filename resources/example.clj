(ns example
  (:require [imminent.core                 :as immi]
            [teams-connector-clj.graph-api :as graph]
            [teams-connector-clj.api       :as api]
            [teams-connector-clj.graph-api :as graph]
            [clojure.pprint                :refer [pprint] :rename {pprint p}]
            [ring.util.codec :as codec]))


;; Get Graph Token
(def token (-> (graph/request-token)
               (immi/await 10000)
               (immi/dderef)))

;; Ingestion code. It also subscribes to channel changes
(def ingest-result (api/ingest-channel "a696037e-ad04-41ce-b8f2-f873adbc22d2"
                                       "19:c92e21150057454ba5fbcb2e26d9e40b@thread.skype"))

(p ingest-result)


;; Retrieve existing channels for team
(def channels (-> (graph/channels "a696037e-ad04-41ce-b8f2-f873adbc22d2" token)
                  (immi/await 10000)
                  (immi/dderef)))
(->> channels
     :value
     (map (juxt :display-name :id)))


;; Re-hydrate message
(-> (slurp (clojure.java.io/resource "message.txt"))
    codec/form-decode
    p)



;; Begin subscription


(def subscription (graph/subscribe-to-channel "a696037e-ad04-41ce-b8f2-f873adbc22d2"
                                              "19:c92e21150057454ba5fbcb2e26d9e40b@thread.skype"
                                              token))

(def channels (-> (graph/channels "a696037e-ad04-41ce-b8f2-f873adbc22d2" token)
                  (immi/await 10000)
                  (immi/dderef)))
(->> channels
     :value
     (map (juxt :display-name :id)))
