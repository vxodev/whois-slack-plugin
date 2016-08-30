(ns vxodev-whois-slack.handler
  (:require [compojure
             [core :refer :all]
             [route :as route]]
            [ring.middleware
             [defaults :refer [api-defaults wrap-defaults]]
             [json :as json]]))

;; Dummy DB
(def db (atom {}))
(defn get-whois [db user]
  (get @db user))
(defn update-whois [db user text]
  (swap! db assoc user text))

(defn validate-param
  "Middleware that checks that a +param+ is == +expected+"
  [handler param expected]
  (fn [req]
    (do
      (assert (= expected (get-in req [:params param]))
              (format "Illegal param value for %s" param))
      (handler req))))

;; TODO: Implement me.
(defn parse-text [text user_name] nil)
(defn render-get [user] nil)
(defn render-set [user text] nil)
(defn render-help [] "")

(defroutes app-routes
  (POST "/" {{:keys [text user_name]} :params}
        (let [parsed (parse-text text user_name)]
          (condp = (:cmd parsed)
            :get (render-get user_name)
            :set (render-set user_name text)
            (render-help))))
  (route/not-found "Not Found"))

(def app (wrap-defaults
          (-> app-routes
              (validate-param :token "xyz")
              (validate-param :command "/whois")
              (validate-param :channel_name "general")
              (validate-param :token "xyz")
              (validate-param :team_id "vxodev"))
          api-defaults))
