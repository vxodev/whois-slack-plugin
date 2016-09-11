(ns vxodev-whois-slack.app
  (:require [ring.adapter.jetty :as jetty]
            [vxodev-whois-slack.handler :as handler]
            [environ.core :refer [env]]))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty handler/app {:port port :join? false})))
