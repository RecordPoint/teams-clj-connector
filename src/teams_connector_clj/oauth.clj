(ns teams-connector-clj.oauth
  (:require [clj-http.client        :as    http]
            [cheshire.core          :as    json]
            [environ.core           :refer [env]]))

(def oauth-params {:grant_type    "client_credentials"
                   :client_id     (env :client-id)
                   :client_secret (env :client-secret)
                   :resource      (env :audience)})

(defn request-token []
  (let [resp (http/post (env :oauth-endpoint)
                        {:form-params oauth-params})]
    (-> resp
        :body
        (json/parse-string true)
        :access_token)))

