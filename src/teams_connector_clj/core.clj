(ns teams-connector-clj.core
  (:require [compojure.core    :refer :all]
            [compojure.route   :as route]
            [compojure.handler :as handler]
            [environ.core      :refer [env]]
            [ring.util.response :refer [response header status]]
            [taoensso.timbre :as timbre :refer [info debug error]]
            [teams-connector-clj.api :as api])
  (:use ring.adapter.jetty))

(defroutes api-routes
  (context "/api/graph/subscriptions" []
           (POST "/notifications" [validationToken] (api/validate-graph-token validationToken))))

(defroutes app
  (GET "/healthcheck" [] (response "Ok."))
  (handler/api api-routes)
  (route/not-found "<h1>Page not found</h1>"))

(defn -main
  [& args]
  (run-jetty app {:port (Integer/parseInt (env :api-port))}))
