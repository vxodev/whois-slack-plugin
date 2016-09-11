(ns vxodev-whois-slack.handler
  (:require [clojure.java.jdbc :as sql]
            [compojure
             [core :refer :all]
             [route :as route]]
            [environ.core :as env :refer [env]]
            [migratus.core :as migratus]
            [ring.middleware
             [defaults :refer [api-defaults wrap-defaults]]]
            [ring.util.response :refer [content-type response]]))

(defprotocol Repository
  "Getting and storing whois-entries are done through this protocol.
  An entry is defined as:
  {
    :nick <Slack user_name>
    :text <The whois text>
    :updated-at <LocalDateTime when the entry was updated>
    :created-at <LocalDateTime when the entry was initially created>
  }"
  (get-entry [repo channel nick] "Get an entry, or nil if there is none.")
  (put-entry [repo channel nick text] "Puts an entry into the repo"))

(defrecord LocalRepo [data-ref]
  Repository
  (get-entry [this _ nick] (get @data-ref nick))
  (put-entry [this _ nick text]
    (swap! data-ref update nick
           (fn [e] {:nick nick
                    :text text
                    :created-at (if (nil? e) "now" (:created-at e))
                    :updated-at "now"}) )))

(defn- db-get-entry-sql []
  (let [tformat "YYYY-MM-DD HH24:MI:SS"
        sql (format "SELECT
                      data as \"text\",
                      to_char(created_at, '%s') as \"created-at\",
                      to_char(updated_at, '%s') as \"updated-at\"
                     FROM whois
                     WHERE nick = ? and channel = ?"
                    tformat, tformat)]
    sql))
(defn db-put-entry-vec [channel nick text]
  ["insert into whois as w (nick, channel, data, created_at, updated_at) values (?, ?, ?, now(), now()) on conflict (nick, channel) do update set data = ?, updated_at = now() where w.nick = ? and w.channel = ?"
   nick, channel, text, text, nick, channel])

(defrecord DbRepo [db]

  Repository

  (get-entry [this channel nick]
    (let [tformat "YYYY-MM-DD HH24:MI:SS"
          recs (sql/query db [(db-get-entry-sql) nick channel])]
      (if-let [rec (first recs)]
        (merge rec {:nick nick}))))

  (put-entry [this channel nick text]
    (sql/execute! db (db-put-entry-vec channel nick text))))


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
      #"--set\s+(.*)" :>> (fn [[_ t]] {:cmd :set :text t :user_name user_name})
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
(defn render-help [] "HELP HELP")

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

(defn create-repo
  "If :database-url is defined - return a migrated DbRepo.
  Otherwise use the in-memory repository."
  []
  (if-let [db-url (env :database-url)]
    (do
      (migratus/migrate {:store :database
                         :migration-dir "migrations/"
                         :migration-table-name "migrations"
                         :db db-url})
      (->DbRepo db-url))
    (->LocalRepo (atom {}))))

(def app
  (wrap-defaults
   (-> (create-routes (create-repo))
       (validate-param :token (env :slack-token))
       (validate-param :command "/whois"))
   api-defaults))
