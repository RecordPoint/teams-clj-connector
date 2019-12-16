(ns teams-connector-clj.record
  (:require [clojure.spec.alpha :as s])
  (:import  [java.time.format DateTimeFormatter]))

(def formatter         (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
(def date-time-pattern (re-pattern "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}Z"))

(s/def ::content-version           string?)
(s/def ::mime-type                 string?)
(s/def ::parent-external-id        string?)
(s/def ::source-last-modified-date (s/and string? #(re-matches date-time-pattern %)))
(s/def ::title                     string?)
(s/def ::author                    string?)
(s/def ::source-created-by         string?)
(s/def ::source-created-date       (s/and string? #(re-matches date-time-pattern %)))
(s/def ::external-id               string?)
(s/def ::connector-id              string?)
(s/def ::location                  string?)
(s/def ::media-type                #{"Electronic"})

(s/def ::record (s/keys :req-un [::source-last-modified-by
                                 ::content-version
                                 ::mime-type
                                 ::parent-external-id
                                 ::source-last-modified-date
                                 ::title
                                 ::author
                                 ::source-created-by
                                 ::source-created-date
                                 ::external-id
                                 ::connector-id
                                 ::location
                                 ::media-type
                                 ]
                        :opt []))


(defn record [record-map]
  (if (= (s/conform ::record record-map) ::s/invalid)
    (throw (ex-info "Invalid input" (s/explain-data ::record record-map)))
    record-map))

