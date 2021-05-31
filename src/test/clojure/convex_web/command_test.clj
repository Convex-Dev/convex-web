(ns convex-web.command-test
  (:require [clojure.test :refer :all]

            [ring.mock.request :as mock]

            [convex-web.specs]
            [convex-web.command :as c]
            [convex-web.convex :as convex]
            [convex-web.test :refer :all]
            [convex-web.encoding :as encoding]
            [convex-web.web-server :as web-server])
  (:import (convex.core.data StringShort)))

(def context (make-convex-context))

(def system nil)

(use-fixtures :each (make-system-fixture #'system))

(deftest transact-mode-test
  (testing "Rollback"

    (let [handler (web-server/site system)

          response (handler (mock/request :post "/api/internal/generate-account"))

          account (encoding/transit-decode-string (get response :body))

          response (handler (mock/request :post "/api/internal/confirm-account"
                              (encoding/transit-encode (:convex-web.account/address account))))

          command1 (c/execute system #:convex-web.command {:mode :convex-web.command.mode/transaction
                                                           :address (:convex-web.account/address account)
                                                           :transaction
                                                           #:convex-web.transaction
                                                               {:source "(def x 1)"
                                                                :type :convex-web.transaction.type/invoke
                                                                :language :convex-lisp}})

          command2 (c/execute system #:convex-web.command {:mode :convex-web.command.mode/transaction
                                                           :address (:convex-web.account/address account)
                                                           :transaction
                                                           #:convex-web.transaction
                                                               {:source "(do (def x 2) (rollback :abort))"
                                                                :type :convex-web.transaction.type/invoke
                                                                :language :convex-lisp}})

          command3 (c/execute system #:convex-web.command {:mode :convex-web.command.mode/transaction
                                                           :address (:convex-web.account/address account)
                                                           :transaction
                                                           #:convex-web.transaction
                                                               {:source "x"
                                                                :type :convex-web.transaction.type/invoke
                                                                :language :convex-lisp}})]

      (is (= 1 (:convex-web.command/object command1)))
      (is (= :abort (:convex-web.command/object command2)))
      (is (= 1 (:convex-web.command/object command3))))))

(deftest query-mode-test
  (testing "Simple Commands"
    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/address 9
                                     ::c/query
                                     {:convex-web.query/source "1"
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/success
              ::c/object 1}
            (select-keys command [::c/status ::c/object]))))

    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/address 9
                                     ::c/query
                                     {:convex-web.query/source "1.0"
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/success
              ::c/object 1.0}
            (select-keys command [::c/status ::c/object]))))

    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/address 9
                                     ::c/query
                                     {:convex-web.query/source "\"Hello\""
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/success
              ::c/object "Hello"}
            (select-keys command [::c/status ::c/object])))))

  (testing "Symbol lookup"
    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/address 9
                                     ::c/query
                                     {:convex-web.query/source "inc"
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/success
              ::c/object "inc"}
            (select-keys command [::c/status ::c/object])))))

  (testing "Lookup doc"
    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/address 9
                                     ::c/query
                                     {:convex-web.query/source "(doc inc)"
                                      :convex-web.query/language :convex-lisp}})]

      (is (= '#:convex-web.command{:object
                                   {:description "Increments the given number by 1. Converts to Long if necessary."
                                    :errors {:CAST "If the actor argument is not a Number."}
                                    :examples [{:code "(inc 10)"}]
                                    :signature [{:params [num]
                                                 :return Long}]
                                    :type :function}
                                   :status :convex-web.command.status/success}
            (select-keys command [::c/status ::c/object])))))

  (testing "Syntax error"
    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/query
                                     {:convex-web.query/source "("
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/error
              ::c/error {:message "Error while parsing action 'Input/ExpressionList/ZeroOrMore/Sequence/Expression/FirstOf/DelimitedExpression/DataStructure/List/FirstOf/Sequence/List_Action1' at input position (line 1, pos 3):\n(\n  ^\n\nconvex.core.exceptions.ParseException: Expected closing ')'"}}
            (select-keys command [::c/status ::c/object ::c/error])))))

  (testing "Cast error"
    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/address 9
                                     ::c/query
                                     {:convex-web.query/source "(map inc 1)"
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/error
              ::c/object "Can't convert value of type Long to type Sequence"
              ::c/error
              {:code :CAST
               :message "Can't convert value of type Long to type Sequence"
               :trace nil}}
            (select-keys command [::c/status ::c/object ::c/error]))))))

(deftest sandbox-result-test
  (testing "Long"
    (is (= 1 (c/sandbox-result (convex/execute context 1)))))

  (testing "Double"
    (is (= 1.0 (c/sandbox-result (convex/execute context 1.0)))))

  (testing "Address"
    (is (= {:address 9}
          (c/sandbox-result (convex/execute context *address*)))))

  (testing "Blob"
    (is (= {:hex-string "0000000000000009"
            :length 8}
          (c/sandbox-result (convex/execute context (blob *address*))))))

  (testing "StringShort"
    ;; Defaults to datafy.
    (is (= "X" (c/sandbox-result (StringShort/create "X"))))))

(deftest result-metadata-test
  (testing "Nil"
    (is (= {} (c/result-metadata (convex/execute context nil)))))

  (testing "Boolean"
    (is (= {:type :boolean} (c/result-metadata (convex/execute context true))))
    (is (= {:type :boolean} (c/result-metadata (convex/execute context false)))))

  (testing "Long"
    (is (= {:type :long} (c/result-metadata (convex/execute context 1)))))

  (testing "Double"
    (is (= {:type :double} (c/result-metadata (convex/execute context 1.0)))))

  (testing "String"
    (is (= {:type :string} (c/result-metadata (convex/execute context "Hello")))))

  (testing "Map"
    (is (= {:type :map} (c/result-metadata (convex/execute context {}))))
    (is (= {:type :map} (c/result-metadata (convex/execute context {:a 1})))))

  (testing "List"
    (is (= {:type :list} (c/result-metadata (convex/execute context '(1 2 3))))))

  (testing "Vector"
    (is (= {:type :vector} (c/result-metadata (convex/execute context [1 2 3])))))

  (testing "Set"
    (is (= {:type :set} (c/result-metadata (convex/execute context #{})))))

  (testing "Address"
    (is (= {:type :address} (c/result-metadata (convex/execute context *address*)))))

  (testing "Blob"
    (is (= {:type :blob} (c/result-metadata (convex/execute context (blob *address*))))))

  (testing "Symbol"
    (is (= {:type :symbol} (c/result-metadata (convex/execute context 's)))))

  (testing "Function"
    (is (= {} (c/result-metadata (convex/execute context inc))))
    (is (= '{:doc {:description "Increments the given number by 1. Converts to Long if necessary."
                   :errors {:CAST "If the actor argument is not a Number."}
                   :examples [{:code "(inc 10)"}]
                   :signature [{:params [num]
                                :return Long}]
                   :symbol "inc"
                   :type :function}
             :type :function} (c/result-metadata (convex/execute context inc) {:source "inc" :lang :convex-lisp}))))

  (testing "Macro"
    (is (= {} (c/result-metadata (convex/execute context defn))))
    (is (= '{:doc {:description "Defines a function in the current environment."
                   :examples [{:code "(defn my-square [x] (* x x))"}]
                   :signature [{:params [name
                                         params
                                         &
                                         body]}
                               {:params [name
                                         &
                                         fn-decls]}]
                   :symbol "defn"
                   :type :macro}
             :expander true
             :type :macro}
          (c/result-metadata (convex/execute context defn) {:source "defn" :lang :convex-lisp}))))

  (testing "Special"
    (is (= {:type :symbol} (c/result-metadata (convex/execute context def))))
    (is (= '{:doc {:description "Creates a definition in the current environment. This value will persist in the environment owned by the current account. The name argument must be a Symbol, or a Symbol wrapped in a Syntax Object with optional metadata."
                   :errors {:CAST "If the argument is neither a valid Symbol name nor a Syntax containing a Symbol value."}
                   :examples [{:code "(def a 10)"}]
                   :signature [{:params [name
                                         value]}]
                   :symbol "def"
                   :type :special}
             :type :special}
          (c/result-metadata (convex/execute context def) {:source "def" :lang :convex-lisp})))))


(comment
  (require 'convex-web.command-test)
  (in-ns 'convex-web.command-test)

  (run-tests))