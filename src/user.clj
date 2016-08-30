(ns user "User-level REPL tools"
    (:require [ring.adapter.jetty :as jetty]
              [vxodev-whois-slack.handler :as handler]))

(defonce server-ref (atom nil))


(defn start-server
  "Starts the Jetty server"
  ([] (start-server 8888))
  ([port]
   (swap! server-ref #(if (nil? %)
                        (jetty/run-jetty #'handler/app {:port port :join? false})
                        %))))

(defn stop-server
  "Stops the running Jetty server"
  []
  (swap! server-ref #(when (not (nil? %))
                       (.stop %))))

