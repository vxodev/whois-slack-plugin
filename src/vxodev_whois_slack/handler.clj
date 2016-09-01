(ns vxodev-whois-slack.handler
  (:require [compojure
             [core :refer :all]
             [route :as route]]
            [ring.middleware
             [defaults :refer [api-defaults wrap-defaults]]
             [json :as json]]))


(defprotocol Repository
  "TODO: Maybe also store the channel from where the entry was updated?
  Getting and storing whois-entries are done through this protocol.
  An entry is defined as:
  {
    :nick <Slack user_name>
    :text <The whois text>
    :updated-at <LocalDateTime when the entry was updated>
    :created-at <LocalDateTime when the entry was initially created>
  }"
  (get-entry [repo nick] "Get an entry, or nil if there is none.")
  (put-entry [repo nick text] "Puts an entry into the repo")
  (all-entries [repo] "Gets all entries stored in the repo"))

;; TODO: Can records have a "constructor"?

(defrecord LocalRepo [data-ref]
  Repository
  (get-entry [this nick] (get @data-ref nick))
  (put-entry [this nick text]
    (swap! data-ref update nick
           (fn [e] {:nick nick
                    :text text
                    :created-at (if (nil? e) "now" (:created-at e))
                    :updated-at "now"}) ))
  (all-entries [this] (vals @data-ref)))

(defn validate-param
  "Middleware that checks that a +param+ is == +expected+"
  [handler param expected]
  (fn [req]
    (do
      (assert (= expected (get-in req [:params param]))
              (format "Illegal param value for %s" param))
      (handler req))))

;; TODO: Implement me.
(defn parse-text [text user_name]
    (condp re-find text
      #"--set\s+(.*)" :>> (fn [[_ t]] {:cmd :set :text t :user_name user_name})
      #"@(\w+)"       :>> (fn [[_ t]] {:cmd :get :nick t :user_name user_name})
      {:cmd :help}))
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
