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

(defn- graph-get [uri token]
  (-> (http/get uri
                {:content-type  :json
                 :accept        :json
                 :oauth-token   token})
      :body
      (json/parse-string true)))


(defn request-token []
  (-> (http/post (env :oauth-endpoint-v2) {:form-params oauth-params})
      :body
      (json/parse-string true)
      :access_token))

(defn channels [team-id token]
  (->> (graph-get (graph-uri "/v1.0/teams/%s/channels" team-id)
                  token)
       :value
       (map (fn [channel]
              {:id          (:id channel)
               :name        (:displayName channel)
               :description (:description channel)}))))

(defn messages [team-id channel-id token]
  (->> (graph-get (graph-uri "/beta/teams/%s/channels/%s/messages" team-id channel-id)
                  token)
       :value
       (map (fn [message]
              {:id           (:id message)
               :message-type (:messageType message)
               :from         {:id   (get-in (:from message) [:user :id])
                              :name (get-in (:from message) [:user :displayName])}
               :created-at   (:createdDateTime message)
               :body         {:content-type (:body message)}}))))
