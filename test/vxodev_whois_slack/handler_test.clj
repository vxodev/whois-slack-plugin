(ns vxodev-whois-slack.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [vxodev-whois-slack.handler :refer :all]
            [vxodev-whois-slack.handler :as handler]))


;; TODO: Create handler using a function to allow the DB to be injected as a dependency.
;; Write tests that verify the full stack.

(deftest text-parsing
  (is (= {:cmd :get :nick "nick" :user_name "user"}
         (parse-text "@nick" "user")))
  (is (= {:cmd :set :user_name "foo" :text "Foo bar zip zap"}
         (parse-text "set Foo bar zip zap" "foo")))
  (is (= {:cmd :help}
         (parse-text "" "sdjfh")))
  (is (= {:cmd :help}
         (parse-text "alsdkjhfakjshdf" "sjdfh"))))


(let [app (handler/app {:repo (handler/->LocalRepo (atom {:nick "anders" :text "Hello, here I am"}))
                        :slack-token "ABC"
                        :command "/whois"})]
  (-> (mock/request :post "/" {:command "/whois"
                               :channel_name "vxodev"
                               :user_name "anders"
                               :text "@anders"})
      app))
