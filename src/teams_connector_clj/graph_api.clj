(ns teams-connector-clj.graph-api
  (:require [clj-http.client        :as    http]
            [environ.core           :refer [env]]
            [cheshire.core          :as    json]))

(def oauth-params {:grant_type    "client_credentials"
                   :client_id     (env :client-id)
                   :client_secret (env :client-secret)
                   :scope         "https://graph.microsoft.com/.default"})

(defn- graph-uri [path & params]
  (let [endpoint (str (env :graph-api-endpoint) path)]
    (apply format endpoint params)))


(defn request-token []
  (-> (http/post (env :oauth-endpoint-v2) {:form-params oauth-params})
      :body
      (json/parse-string true)
      :access_token))

(defn channels [team-id token]
  (-> (http/get (graph-uri "/v1.0/teams/%s/channels" team-id)
                {:content-type  :json
                 :accept        :json
                 :oauth-token   token})
      :body
      (json/parse-string true)))

(defn messages [team-id channel-id token]
  (-> (http/get (graph-uri "/beta/teams/%s/channels/%s/messages" team-id channel-id)
                {:content-type  :json
                 :accept        :json
                 :oauth-token   token})
      :body
      (json/parse-string true)))
