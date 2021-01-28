(ns convex-web.specs-test
  (:require [convex-web.specs]

            [clojure.test :refer :all]
            [clojure.spec.alpha :as s]

            [expound.alpha :as expound])
  (:import (convex.core Init)))

(set! s/*explain-out* expound/printer)

(s/check-asserts true)

(def HERO-address (.longValue Init/HERO))

(deftest command-specs
  (testing "Incoming Query"
    (let [q #:convex-web.query {:source "1"
                                :language :convex-lisp}

          c #:convex-web.command {:mode :convex-web.command.mode/query
                                  :query q}]
      (is (s/assert :convex-web/command c)))

    (let [q #:convex-web.query {:source "1"
                                :language :convex-lisp
                                :address HERO-address}

          c #:convex-web.command {:mode :convex-web.command.mode/query
                                  :query q}]
      (is (s/assert :convex-web/command c))))

  (testing "Incoming Transaction"
    (let [t #:convex-web.transaction{:type :convex-web.transaction.type/invoke
                                     :source "1"
                                     :language :convex-lisp
                                     :target 1}

          c #:convex-web.command {:address HERO-address
                                  :mode :convex-web.command.mode/transaction
                                  :transaction t}]
      (is (s/assert :convex-web/command c)))

    (let [t #:convex-web.transaction{:type :convex-web.transaction.type/transfer
                                     :amount 1
                                     :target 1}

          c #:convex-web.command {:address HERO-address
                                  :mode :convex-web.command.mode/transaction
                                  :transaction t}]
      (is (s/assert :convex-web/command c))))

  (testing "Running Transaction"
    (let [t #:convex-web.transaction {:type :convex-web.transaction.type/invoke
                                      :source "1"
                                      :language :convex-lisp
                                      :target 1}

          c #:convex-web.command {:id 1
                                  :address HERO-address
                                  :status :convex-web.command.status/running
                                  :mode :convex-web.command.mode/transaction
                                  :transaction t}]
      (is (s/assert :convex-web/command c))))

  (testing "Running Query"
    (let [q #:convex-web.query {:source "1"
                                :language :convex-lisp
                                :address HERO-address}

          c #:convex-web.command {:id 1
                                  :status :convex-web.command.status/running
                                  :mode :convex-web.command.mode/query
                                  :query q}]
      (is (s/assert :convex-web/command c))))

  (testing "Successful Query"
    (let [q #:convex-web.query {:source "1"
                                :language :convex-lisp
                                :address HERO-address}

          c #:convex-web.command {:id 1
                                  :status :convex-web.command.status/success
                                  :mode :convex-web.command.mode/query
                                  :query q
                                  :object 1}]
      (is (s/assert :convex-web/command c))))

  (testing "Error Query"
    (let [q #:convex-web.query {:source "1"
                                :language :convex-lisp
                                :address HERO-address}

          c #:convex-web.command {:id 1
                                  :status :convex-web.command.status/error
                                  :mode :convex-web.command.mode/query
                                  :query q
                                  :error {:message "Error"}}]
      (is (s/assert :convex-web/command c)))))
