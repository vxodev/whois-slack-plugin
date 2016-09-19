(ns vxodev-whois-slack.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [vxodev-whois-slack.handler :refer :all]
            [vxodev-whois-slack.handler :as handler]
            [vxodev-whois-slack.repo :refer :all]))

(deftest text-parsing
  (is (= {:cmd :get :nick "nick" :user_name "user"}
         (parse-text "@nick" "user")))
  (is (= {:cmd :set :user_name "foo" :text "Foo bar zip zap"}
         (parse-text "set Foo bar zip zap" "foo")))
  (is (= {:cmd :help}
         (parse-text "" "sdjfh")))
  (is (= {:cmd :help}
         (parse-text "alsdkjhfakjshdf" "sjdfh"))))

(defn- fresh-app
  "Creates a new app-handler"
  []
  (let [repo (->LocalRepo (atom {}))
        app  (handler/app {:repo        repo
                           :slack-token "foo"
                           :command     "/whois"})]
    app))

(defn- post-req
  "Sends a request to the app"
  [app user text]
  (app (mock/request :post "/" {:text text
                                :command "/whois"
                                :token "foo"
                                :user_name user})))

(deftest web-api
  (testing "happy-path"
    (let [app (fresh-app)]

      (let [resp  (post-req app "moi" "set Hello World")]
       (is (= 200 (:status resp)))
       (is (= "Whois entry updated for @moi" (:body resp))))

      (let [resp (post-req app "foo" "@moi")]
        (is (re-find #"Hello World" (:body resp)))
        (is (= 200 (:status resp)))))))

