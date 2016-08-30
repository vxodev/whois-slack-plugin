(ns vxodev-whois-slack.handler
  (:require [compojure
             [core :refer :all]
             [route :as route]]
            [ring.middleware
             [defaults :refer [api-defaults wrap-defaults]]
             [json :as json]]))

(defroutes app-routes
  (GET "/" [] {:body {:foo "bar" :zip "zap" :width 300}})
  (route/not-found "Not Found"))

(def app
  (wrap-defaults
   (-> app-routes
       (json/wrap-json-body :keywords? true)
       (json/wrap-json-response :pretty true))
   api-defaults))
