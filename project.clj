(defproject vxodev-whois-slack "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [postgresql "9.3-1102.jdbc41"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [migratus "0.8.29"]
                 [environ "1.1.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler vxodev-whois-slack.handler/app
         :nrepl {:start? true
                 :port 9998}}
  :profiles
  {:dev {:dependencies [[ring/ring-jetty-adapter "1.5.0"]
                        [javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
