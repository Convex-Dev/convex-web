(ns convex-web.specs-test
  (:require [convex-web.specs]
            [convex-web.test :refer [catchy]]

            [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]))

(set! s/*explain-out* expound/printer)

(s/check-asserts true)

(deftest command-specs
  (testing "Incoming Query"
    (let [q #:convex-web.query {:source "1"
                                :language :convex-lisp}

          c #:convex-web.command {:mode :convex-web.command.mode/query
                                  :query q}]
      (is (catchy (s/assert :convex-web/command c))))

    (let [q #:convex-web.query {:source "1"
                                :language :convex-lisp
                                :address "ABC"}

          c #:convex-web.command {:mode :convex-web.command.mode/query
                                  :query q}]
      (is (catchy (s/assert :convex-web/command c)))))

  (testing "Incoming Transaction"
    (let [t #:convex-web.transaction{:type :convex-web.transaction.type/invoke
                                     :source "1"
                                     :language :convex-lisp
                                     :target "B5cb456779DF23F1032df9C594eec3b3C284987f5735218cFfa422dC07CFf8E0"}

          c #:convex-web.command {:address "B5cb456779DF23F1032df9C594eec3b3C284987f5735218cFfa422dC07CFf8E0"
                                  :mode :convex-web.command.mode/transaction
                                  :transaction t}]
      (is (catchy (s/assert :convex-web/command c))))

    (let [t #:convex-web.transaction{:type :convex-web.transaction.type/transfer
                                     :amount 1
                                     :target "B5cb456779DF23F1032df9C594eec3b3C284987f5735218cFfa422dC07CFf8E0"}

          c #:convex-web.command {:address "B5cb456779DF23F1032df9C594eec3b3C284987f5735218cFfa422dC07CFf8E0"
                                  :mode :convex-web.command.mode/transaction
                                  :transaction t}]
      (is (catchy (s/assert :convex-web/command c)))))

  (testing "Running Transaction"
    (let [t #:convex-web.transaction {:type :convex-web.transaction.type/invoke
                                      :source "1"
                                      :language :convex-lisp
                                      :target "B5cb456779DF23F1032df9C594eec3b3C284987f5735218cFfa422dC07CFf8E0"}

          c #:convex-web.command {:id 1
                                  :address "B5cb456779DF23F1032df9C594eec3b3C284987f5735218cFfa422dC07CFf8E0"
                                  :status :convex-web.command.status/running
                                  :mode :convex-web.command.mode/transaction
                                  :transaction t}]
      (is (catchy (s/assert :convex-web/command c)))))

  (testing "Running Query"
    (let [q #:convex-web.query {:source "1"
                                :language :convex-lisp
                                :address "ABC"}

          c #:convex-web.command {:id 1
                                  :status :convex-web.command.status/running
                                  :mode :convex-web.command.mode/query
                                  :query q}]
      (is (catchy (s/assert :convex-web/command c)))))

  (testing "Successful Query"
    (let [q #:convex-web.query {:source "1"
                                :language :convex-lisp
                                :address "ABC"}

          c #:convex-web.command {:id 1
                                  :status :convex-web.command.status/success
                                  :mode :convex-web.command.mode/query
                                  :query q
                                  :object 1}]
      (is (catchy (s/assert :convex-web/command c)))))

  (testing "Error Query"
    (let [q #:convex-web.query {:source "1"
                                :language :convex-lisp
                                :address "ABC"}

          c #:convex-web.command {:id 1
                                  :status :convex-web.command.status/error
                                  :mode :convex-web.command.mode/query
                                  :query q
                                  :error {:message "Error"}}]
      (is (catchy (s/assert :convex-web/command c))))))
