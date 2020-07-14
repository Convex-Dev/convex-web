(ns convex-web.specs-test
  (:require [convex-web.specs]

            [clojure.test :refer :all]
            [clojure.spec.alpha :as s]))

(deftest command-specs
  (testing "Incoming Query"
    (let [q #:convex-web.query {:source "1"}

          c #:convex-web.command {:mode :convex-web.command.mode/query
                                  :query q}]
      (is (s/valid? :convex-web/command c)))

    (let [q #:convex-web.query {:source "1"
                                :address "ABC"}

          c #:convex-web.command {:mode :convex-web.command.mode/query
                                  :query q}]
      (is (s/valid? :convex-web/command c))))

  (testing "Incoming Transaction"
    (let [t #:convex-web.transaction{:type :convex-web.transaction.type/invoke
                                     :source "1"
                                     :target "ABC"}

          c #:convex-web.command {:address "ABC"
                                  :mode :convex-web.command.mode/transaction
                                  :transaction t}]
      (is (s/valid? :convex-web/command c)))

    (let [t #:convex-web.transaction{:type :convex-web.transaction.type/transfer
                                     :amount 1
                                     :target "ABC"}

          c #:convex-web.command {:address "ABC"
                                  :mode :convex-web.command.mode/transaction
                                  :transaction t}]
      (is (s/valid? :convex-web/command c))))

  (testing "Running Transaction"
    (let [t #:convex-web.transaction {:type :convex-web.transaction.type/invoke
                                      :source "1"
                                      :target "ABC"}

          c #:convex-web.command {:id 1
                                  :address "ABC"
                                  :status :convex-web.command.status/running
                                  :mode :convex-web.command.mode/transaction
                                  :transaction t}]
      (is (s/valid? :convex-web/command c))))

  (testing "Running Query"
    (let [q #:convex-web.query {:source "1"
                                :address "ABC"}

          c #:convex-web.command {:id 1
                                  :status :convex-web.command.status/running
                                  :mode :convex-web.command.mode/query
                                  :query q}]
      (is (s/valid? :convex-web/command c))))

  (testing "Successful Query"
    (let [q #:convex-web.query {:source "1"
                                :address "ABC"}

          c #:convex-web.command {:id 1
                                  :status :convex-web.command.status/success
                                  :mode :convex-web.command.mode/query
                                  :query q
                                  :object 1}]
      (is (s/valid? :convex-web/command c))))

  (testing "Error Query"
    (let [q #:convex-web.query {:source "1"
                                :address "ABC"}

          c #:convex-web.command {:id 1
                                  :status :convex-web.command.status/error
                                  :mode :convex-web.command.mode/query
                                  :query q
                                  :error "Error"}]
      (is (s/valid? :convex-web/command c)))))
