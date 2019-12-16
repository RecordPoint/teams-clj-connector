(ns teams-connector-clj.core)

(comment
  (require '[clj-http.client        :as    http]
           '[cheshire.core          :as    json]
           '[clojure.pprint         :refer [pprint] :rename {pprint p}]
           '[environ.core           :refer [env]]
           '[clojure.spec.alpha     :as    s]
           '[camel-snake-kebab.core :as    csk])

  (import  '(java.time ZonedDateTime ZoneId))
  (require '[teams-connector-clj.record :as r])
  (require '[teams-connector-clj.r365   :as r365])
  (require '[teams-connector-clj.oauth  :as oauth])




  (def token (oauth/request-token))

  (def formatted-now (-> (ZonedDateTime/now (ZoneId/of "UTC"))
                         (.format r/formatter)))

  (def record (r/record {:source-last-modified-by   formatted-now,
                         :content-version           "v0.2",
                         :mime-type                 "text/plain",
                         :parent-external-id        "444",
                         :source-last-modified-date formatted-now,
                         :title                     "My first record",
                         :author                    "Leonardo Borges",
                         :source-created-by         "Leonardo Borges",
                         :source-created-date       formatted-now,
                         :external-id               "777",
                         :connector-id             (env :connector-id),
                         :location                 "/",
                         :media-type               "Electronic"}))




  (r365/submit-record record token)

  )
