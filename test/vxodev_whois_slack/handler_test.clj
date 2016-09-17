(ns vxodev-whois-slack.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [vxodev-whois-slack.handler :refer :all]
            [vxodev-whois-slack.handler :as handler]
            [vxodev-whois-slack.repo :refer :all]))


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

;; LAB
(let [repo (->LocalRepo (atom {}))
      app (handler/app {:repo repo
                        :slack-token "ABC"
                        :command "/whois"})
      params {:command "/whois"
              :token "ABC"
              :channel_name "vxodev"}]
  (do
    (put-entry repo "vxodev" "anders" "Hello World")
    (-> (mock/request :post "/" (assoc params :text "@anders"))
       app)))
