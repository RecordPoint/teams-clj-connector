(ns teams-connector-clj.core)

(comment
  (require '[clj-http.client :as http]
           '[cheshire.core   :as json]
           '[clojure.pprint  :refer [pprint] :rename {pprint p}]
           '[environ.core    :refer [env]])
  

  (import  '(java.time ZonedDateTime ZoneId)
           '(java.time.format DateTimeFormatter))

  (def oauth-params {:grant_type    "client_credentials"
                     :client_id     (env :client-id)
                     :client_secret (env :client-secret)
                     :resource      (env :audience)})

  (def token-resp (http/post (env :oauth-endpoint)
                             {:form-params oauth-params}))

  (def access-token (-> token-resp
                        :body
                        (json/parse-string true)
                        :access_token))


  (def formatter     (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
  (def formatted-now (-> (ZonedDateTime/now (ZoneId/of "UTC"))
                         (.format formatter)))


  (def record {
               "externalId"             "333",
               "parentExternalId"       "222",
               "connectorId"            (env :connector-id),
               "title"                  "My first record",
               "author"                 "Leonardo Borges",
               "mimeType"               "text/plain",
               "sourceLastModifiedDate" formatted-now,
               "sourceLastModifiedBy"   formatted-now,
               "sourceCreatedDate"      formatted-now,
               "sourceCreatedBy"        "Leonardo Borges",
               "contentVersion"         "v0.1",
               "location"               "/",
               "mediaType"              "Electronic"
               })

  ;(p record)


  (def submit-resp (http/post (str (env :connector-endpoint) "/items")
                              {:content-type  :json
                               :accept        :json
                               :oauth-token   access-token
                               :form-params   record}))

  (p (-> submit-resp :body (json/parse-string true)))


  )
