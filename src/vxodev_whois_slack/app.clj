(ns vxodev-whois-slack.app
  (:require [environ.core :refer [env]]
            [migratus.core :as migratus]
            [ring.adapter.jetty :as jetty]
            [vxodev-whois-slack.repo :as repo]
            [vxodev-whois-slack.handler :as handler]))

(defn- create-repo
  "If :database-url is defined - return a migrated DbRepo.
  Otherwise use the in-memory repository."
  []
  (if-let [db-url (env :database-url)]
    (do
      (migratus/migrate {:store :database
                         :migration-dir "migrations/"
                         :migration-table-name "migrations"
                         :db db-url})
      (repo/->DbRepo db-url))

    (repo/->LocalRepo (atom {}))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (handler/app {:repo (create-repo)
                                   :slack-token (env :slack-token)
                                   :command "/whois"})
                     {:port port :join? false})))
