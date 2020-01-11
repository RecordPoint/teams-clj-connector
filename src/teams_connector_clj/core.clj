(ns teams-connector-clj.core
  (:require [compojure.core    :refer :all]
            [compojure.route   :as route]
            [compojure.handler :as handler]
            [environ.core      :refer [env]]
            [ring.util.response :refer [response header status]]
            [ring.middleware.json :refer [wrap-json-body]]
            [taoensso.timbre :as timbre :refer [info debug error]]
            [teams-connector-clj.api :as api]
            [cheshire.core          :as    json])
  (:use ring.adapter.jetty))

(defroutes api-routes
  (context "/api/graph/subscriptions" []
           (POST "/notifications" [validationToken :as {body :body}]
                 (api/handle-graph-subscription-notification body validationToken))))

(defroutes app
  (->  api-routes
       handler/api
       wrap-json-body)
  (GET "/healthcheck" [] (response "Ok."))
  (route/not-found "<h1>Page not found</h1>"))

(defn -main
  [& args]
  (run-jetty app {:port (Integer/parseInt (env :api-port))}))
