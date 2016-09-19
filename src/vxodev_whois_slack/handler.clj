(ns vxodev-whois-slack.handler
  (:require [vxodev-whois-slack.repo :refer [get-entry put-entry]]
            [compojure [core :refer :all] [route :as route]]
            [environ.core :as env :refer [env]]
            [migratus.core :as migratus]
            [ring.middleware
             [defaults :refer [api-defaults wrap-defaults]]]
            [ring.util.response :refer [content-type response]]))

(defn validate-param
  "Middleware that checks that a +param+ is == +expected+"
  [handler param expected]
  (fn [req]
    (do
      (assert (= expected (get-in req [:params param]))
              (format "Illegal param value for %s" param))
      (handler req))))

(defn parse-text [text user_name]
  (condp re-find text
    #"set\s+(.*)" :>> (fn [[_ t]] {:cmd :set :text t :user_name user_name})
    #"@(\w+)"       :>> (fn [[_ t]] {:cmd :get :nick t :user_name user_name})
    {:cmd :help}))

(defn render-get [repo channel user]
  (if-let [rec (get-entry repo channel user)]
    (format "Whois entry for @%s (Created: %s, Updated: %s):\n%s"
            user (:created-at rec) (:updated-at rec) (:text rec))
    (format "No whois entry for @%s in #%s" user channel)))

(defn render-set [repo channel user text]
  (do
    (put-entry repo channel user text)
    (format "Whois entry updated for @%s" user)))
(defn render-help [] "View or update whois-entries for #VXODEV.
To add or update your own record: `/whois set <bla bla bla>`
To view a whois entry: `/whois @<username>`.

Code is here: https://github.com/vxodev/whois-slack-plugin - pull-requests and issues are welcome.")

(defn create-routes [repo]
  (routes
   (POST "/" {{:keys [text user_name channel_name]} :params}
     (content-type (response
                    (let [parsed (parse-text text user_name)]
                      (condp = (:cmd parsed)
                        :get (render-get repo channel_name (:nick parsed))
                        :set (render-set repo channel_name user_name (:text parsed))
                        (render-help)))) "text/plain"))
   (route/not-found "Not Found")))

;; Give the app the config from app.clj

(defn app [{:keys [repo slack-token command]}]
  (wrap-defaults
   (-> (create-routes repo)
       (validate-param :token slack-token)
       (validate-param :command command))
   api-defaults))
