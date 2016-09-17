(ns vxodev-whois-slack.repo
  (:require [clojure.java.jdbc :as sql]))


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
  (let [tformat "YYYY-MM-DD HH24:MI:SS (TZ)"
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
