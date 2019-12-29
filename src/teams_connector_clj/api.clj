(ns teams-connector-clj.api
  (:require [ring.util.response :refer [response header status]]
            [taoensso.timbre :as timbre :refer [info debug error]]))


(defn validate-graph-token [token]
  (info (format "Got validation request for token: %s" token))
  (if token
    (-> (response token)
        (header "content-type" "text/plain"))
    (-> (response "Bad request")
        (header "content-type" "text/plain")
        (status 400))))

