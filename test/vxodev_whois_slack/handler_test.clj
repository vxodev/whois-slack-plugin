(ns vxodev-whois-slack.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [vxodev-whois-slack.handler :refer :all]))


(deftest text-parsing
  (is (= {:cmd :get :user_name "nick"}
         (parse-text "@nick" "user")))
  (is (= {:cmd :set :user_name "foo" :text "Foo bar zip zap"}
         (parse-text "--set Foo bar zip zap" "foo")))
  (is (= {:cmd :help}
         (parse-text "" "sdjfh")))
  (is (= {:cmd :help}
         (parse-text "alsdkjhfakjshdf" "sjdfh"))))
