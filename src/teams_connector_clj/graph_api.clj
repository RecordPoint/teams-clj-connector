(ns teams-connector-clj.graph-api
  (:require [clj-http.client        :as    http]
            [environ.core           :refer [env]]
            [cheshire.core          :as    json]
            [camel-snake-kebab.core :as    csk]
            [taoensso.timbre        :as timbre :refer [info debug error]]
            [teams-connector-clj.util          :refer [date-time-formatter]])
  (:import  [java.time ZoneId LocalDateTime Instant]))

(def oauth-params {:grant_type    "client_credentials"
                   :client_id     (env :client-id)
                   :client_secret (env :client-secret)
                   :scope         "https://graph.microsoft.com/.default"})

(defn- graph-uri [path & params]
  (let [endpoint (str (env :graph-api-endpoint) path)]
    (apply format endpoint params)))

(defn- graph-get [uri token]
  (let [r (-> (http/get uri
                        {:content-type  :json
                         :accept        :json
                         :oauth-token   token})
              :body
              (json/parse-string (fn [k]
                                   (let [k (if (.startsWith k "@odata")
                                             (->  k (clojure.string/replace #"@odata\." "odata-"))
                                             k)]
                                     (csk/->kebab-case-keyword k)))))]
    (info (keys  r))
    r))


(defn request-token []
  (-> (http/post (env :oauth-endpoint-v2) {:form-params oauth-params})
      :body
      (json/parse-string true)
      :access_token))

(defn channels [team-id token]
  (graph-get (graph-uri "/v1.0/teams/%s/channels" team-id)
             token))

(defn message [team-id channel-id message-id token]
  (graph-get (graph-uri "/beta/teams/%s/channels/%s/messages/%s" team-id channel-id message-id)
             token))

(defn reduce-rs
  "Paginates through a Graph API resultset by recursively iterating over its `next-page` attribute.
  If no `acc` is given, it will invoke `f` without arguments to produce a seed value.

  Results are combined using `f`. Once there are no more pages, the final accumulated value, `acc` is returned"
  ([f next-page token]
   (let [{next-page :odata-next-link
          payload   :value}           (graph-get next-page token)]
     (reduce-rs f (f (f) payload) next-page token)))
  ([f acc next-page token]
   (if next-page
     (let [{next-page :odata-next-link
            payload   :value}           (graph-get next-page token)]
       (recur f (f acc payload) next-page token))
     acc)))


(defn messages [team-id channel-id token]
  (reduce-rs concat
             (graph-uri "/beta/teams/%s/channels/%s/messages" team-id channel-id)
             token))

(defn replies [team-id channel-id message-id token]
  (reduce-rs concat
             (graph-uri "/beta/teams/%s/channels/%s/messages/%s/replies" team-id channel-id message-id)
             token))

(defn subscribe-to-channel [team-id channel-id token]
  (http/post (graph-uri "/beta/subscriptions")
             {:content-type  :json
              :accept        :json
              :oauth-token token
              :form-params {:changeType         "created,updated"
                            :notificationUrl    "https://ec9e75ef.ngrok.io/api/graph/subscriptions/notifications"
                            :resource           (format  "/teams/%s/channels/%s/messages" team-id channel-id)
                            :expirationDateTime (.format  (.plusMinutes (LocalDateTime/now (ZoneId/of "UTC")) 10)
                                                          date-time-formatter)
                            :clientState        "optional"}}))
