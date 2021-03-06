(ns teams-connector-clj.api
  (:require [ring.util.response :refer [response header status]]
            [teams-connector-clj.graph-api :as graph]
            [teams-connector-clj.r365 :as r365]
            [teams-connector-clj.record :as r]
            [taoensso.timbre :as timbre :refer [info debug error]]
            [imminent.core :as immi]
            [environ.core        :refer [env]]
            [ring.util.codec     :as codec]))


(defn message->record [team channel message]
  (let [title (let [content (get-in message [:body :content])]
                (str (subs content 0 (min (count content) 25))
                     "..."))]
    (r/record {:source-last-modified-by   (get-in message [:from :user :display-name]),
               :content-version           (:etag message)
               :mime-type                 "text/plain",
               :parent-external-id        (codec/form-encode
                                           (format "teams:%s:channels:%s" (:id team) (:id channel)))
               :source-last-modified-date (or (:last-modified-date-time message)
                                              (:created-date-time message))
               :title                     title
               :author                    (get-in message [:from :user :display-name])
               :source-created-by         (get-in message [:from :user :display-name]),
               :source-created-date       (:created-date-time message),
               :external-id               (str "messageid-" (:id message))

               :connector-id             (env :connector-id),
               :location                 (format "%s > %s" (:display-name team) (:display-name channel))
               :media-type               "Electronic"})))

(defn message->binary [team channel message]
  (let [external-id (str "messageid-" (:id message))]
    (r/binary {:connector-id        (env :connector-id)
               :item-external-id    external-id
               :binary-external-id  (str "chatjson-" external-id)})))

(defn submit-record [team channel message rp-token]
  (immi/mdo [_ (r365/submit-record (message->record team channel message) rp-token)
             _ (immi/future (Thread/sleep 5000))
             _ (r365/submit-binary (message->binary team channel message) message rp-token)]
            (do
              (info (format "Message '%s' submitted" (:id message)))
              (immi/const-future :ok))))

(defn process-subscription-event [{type         "changeType"
                                   resource-uri "resource"
                                   :as event} graph-token]
  (let [{team-id    "teams"
         channel-id "channels"
         message-id "messages"} (graph/parse-resource-uri resource-uri)]
    (immi/mdo [[message replies team
                channel rp-token]      (immi/sequence [(graph/message team-id channel-id message-id graph-token)
                                                       (graph/replies team-id channel-id message-id graph-token)
                                                       (graph/team team-id graph-token)
                                                       (graph/channel team-id channel-id graph-token)
                                                       (r365/request-token)])]
              (submit-record team channel (assoc message :replies replies) rp-token))))


(defn handle-graph-subscription-notification [payload graph-token]
  (cond
    (and (nil? payload)
         (nil? graph-token)) (-> (response "Bad request")
                                 (header "content-type" "text/plain")
                                 (status 400))
    graph-token              (do (info (str "Subscription validation request: " graph-token))
                                 (-> (response graph-token)
                                     (header "content-type" "text/plain")))
    payload            (do
                         (immi/mdo [graph-token (graph/request-token)]
                                   (process-subscription-event (-> (payload "value") first) graph-token))
                         (-> (response "Accepted")
                             (header "content-type" "text/plain")
                             (status 202)))))

(defn enrich-messages [messages team-id channel-id graph-token]
  (immi/map-future (fn [message]
                     (-> (graph/replies team-id channel-id (:id message) graph-token)
                         (immi/map #(assoc message :replies %))))
                   messages))

(defn ingest-channel [team-id channel-id]
  (-> (immi/mdo [[graph-token rp-token]          (immi/sequence [(graph/request-token)
                                                                 (r365/request-token)])

                 _                       (graph/subscribe-to-channel team-id channel-id graph-token)

                 [messages team channel] (immi/sequence [(graph/messages team-id channel-id graph-token)
                                                         (graph/team team-id graph-token)
                                                         (graph/channel team-id channel-id graph-token)])

                 messages                (enrich-messages messages team-id channel-id graph-token)]

                (immi/map-future #(submit-record team channel % rp-token)
                                 messages))
      (immi/on-failure #(error % (format "Failed to ingest channel '%s'" channel-id)))))






