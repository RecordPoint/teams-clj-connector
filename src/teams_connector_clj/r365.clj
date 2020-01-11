(ns teams-connector-clj.r365
  (:require [clj-http.client        :as    http]
            [environ.core           :refer [env]]
            [camel-snake-kebab.core :as    csk]
            [cheshire.core          :as    json]
            [taoensso.timbre        :as timbre :refer [info debug error]]
            [imminent.core :as immi]))


(def oauth-params {:grant_type    "client_credentials"
                   :client_id     (env :client-id)
                   :client_secret (env :client-secret)
                   :resource      (env :audience)})


(defn request-token []
  (immi/future (-> (http/post (env :oauth-endpoint) {:form-params oauth-params})
                   :body
                   (json/parse-string true)
                   :access_token)))

(defn submit-record [record access-token]
  (let [record (->> record
                    (map (fn [[k v]] [(csk/->camelCaseString k) v]))
                    (into {}))]
    (immi/future (http/post (str (env :connector-endpoint) "/items")
                            {:content-type  :json
                             :accept        :json
                             :oauth-token   access-token
                             :form-params   record}))))


(defn submit-binary [metadata binary access-token]
  (let [query-params (->> metadata
                    (map (fn [[k v]] [(csk/->camelCaseString k) v]))
                    (into {}))]
    (immi/future (http/post (str (env :connector-endpoint) "/binaries")
                            {:content-type  "application/octet-stream"
                             :accept        :json
                             :oauth-token   access-token
                             :query-params  query-params
                             :form-params   binary}))))
