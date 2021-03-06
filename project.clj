(defproject teams-connector-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.10.0"]
                 [cheshire "5.9.0"]
                 [environ "1.1.0"]
                 [camel-snake-kebab "0.4.1"]
                 [compojure "1.6.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-json "0.5.0"]
                 [ring/ring-jetty-adapter "1.8.0"]
                 [ring/ring-mock "0.4.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.leonardoborges/imminent "0.2.1-SNAPSHOT"]
                 [expectations/clojure-test "1.2.1"]
                 [buddy "2.0.0"]]
  :main          teams-connector-clj.core
  :profiles {:test {:dependencies
                    [[ring/ring-mock "0.4.0"]]}}
  :repl-options {:init-ns teams-connector-clj.core})
