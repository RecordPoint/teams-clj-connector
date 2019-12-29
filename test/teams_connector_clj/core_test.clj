(ns teams-connector-clj.core-test
  (:require [clojure.test :refer :all]
            [teams-connector-clj.core :refer :all]
            [ring.mock.request :as mock]))

(deftest app-test
  (testing "Graph API Validation Webhook"
    (testing "Succeeds when any validation token is given"
      (let [validation-token "SAMPLE_TOKEN"
            uri              (format "/api/graph/subscriptions/notifications?validationToken=%s" validation-token)]
        (is (= (app (mock/request :post uri))
               {:status  200
                :headers {"content-type" "text/plain"}
                :body    validation-token}))))

    (testing "Fails when no validation token is given"
      (is (= (app (mock/request :post "/api/graph/subscriptions/notifications"))
             {:status  400
              :headers {"content-type" "text/plain"}
              :body    "Bad request"})))))

