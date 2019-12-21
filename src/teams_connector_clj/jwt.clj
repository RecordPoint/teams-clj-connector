(ns teams-connector-clj.jwt
  (:require [cheshire.core :as json])
  (:import  java.util.Base64
            [java.time ZoneId LocalDateTime Instant]))


(defn decode-token [token]
  (as-> token $
    (clojure.string/split $ #"\.")
    (second $)
    (.decode (Base64/getDecoder) $)
    (String. $)
    (json/decode $ true)))

(defn utc-exp [token]
  (-> (:exp token)
      long
      Instant/ofEpochSecond
      (LocalDateTime/ofInstant (ZoneId/of "UTC"))))
