(ns teams-connector-clj.record
  (:require [clojure.spec.alpha :as s]
            [teams-connector-clj.util :refer [date-time-formatter date-time-pattern]]))

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


(s/def ::item-external-id               string?)
(s/def ::binary-external-id             string?)

(s/def ::binary (s/keys :req-un [::connector-id
                                 ::item-external-id
                                 ::binary-external-id]
                        :opt []))

(defn binary [binary-map]
  (if (= (s/conform ::binary binary-map) ::s/invalid)
    (throw (ex-info "Invalid input" (s/explain-data ::binary binary-map)))
    binary-map))
