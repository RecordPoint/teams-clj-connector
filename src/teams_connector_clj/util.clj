(ns teams-connector-clj.util
  (:import  [java.time.format DateTimeFormatter]))

(def date-time-formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
(def date-time-pattern   (re-pattern "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9][0-9][0-9]?Z"))
