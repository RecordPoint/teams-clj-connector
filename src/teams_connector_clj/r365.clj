(ns teams-connector-clj.r365
  (:require [clj-http.client        :as    http]
            [environ.core           :refer [env]]
            [camel-snake-kebab.core :as    csk]))


(defn submit-record [record access-token]
  (let [record (->> record
                    (map (fn [[k v]] [(csk/->camelCaseString k) v]))
                    (into {}))]
    (http/post (str (env :connector-endpoint) "/items")
               {:content-type  :json
                :accept        :json
                :oauth-token   access-token
                :form-params   record})))


