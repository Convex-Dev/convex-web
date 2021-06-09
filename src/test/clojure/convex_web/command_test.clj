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

      (is (= "1" (get-in command1 [:convex-web.command/result :convex-web.result/value])))
      (is (= ":abort" (get-in command2 [:convex-web.command/result :convex-web.result/value])))
      (is (= "1" (get-in command3 [:convex-web.command/result :convex-web.result/value]))))))

(deftest query-mode-test
  (testing "Simple Commands"
    (let [{::c/keys [status result]} 
          (c/execute system {::c/mode :convex-web.command.mode/query
                             ::c/address 9
                             ::c/query
                             {:convex-web.query/source "1"
                              :convex-web.query/language :convex-lisp}})]
      
      (is (= :convex-web.command.status/success status))
      (is (= {:convex-web.result/type "Long", 
              :convex-web.result/value "1"}
            (select-keys result [:convex-web.result/type
                                 :convex-web.result/value]))))
    
    (let [{::c/keys [status result]}
          (c/execute system {::c/mode :convex-web.command.mode/query
                             ::c/address 9
                             ::c/query
                             {:convex-web.query/source "1.0"
                              :convex-web.query/language :convex-lisp}})]
      
      (is (= :convex-web.command.status/success status))
      (is (= {:convex-web.result/type "Double", 
              :convex-web.result/value "1.0"}
            (select-keys result [:convex-web.result/type
                                 :convex-web.result/value]))))
    
    (let [{::c/keys [status result]}
          (c/execute system {::c/mode :convex-web.command.mode/query
                             ::c/address 9
                             ::c/query
                             {:convex-web.query/source "\"Hello\""
                              :convex-web.query/language :convex-lisp}})]
      
      (is (= :convex-web.command.status/success status))
      (is (= {:convex-web.result/type "String", 
              :convex-web.result/value "Hello"}
            (select-keys result [:convex-web.result/type
                                 :convex-web.result/value])))))
  
  (testing "Symbol lookup"
    (let [{::c/keys [status result]}
          (c/execute system {::c/mode :convex-web.command.mode/query
                             ::c/address 9
                             ::c/query
                             {:convex-web.query/source "inc"
                              :convex-web.query/language :convex-lisp}})]
      
      (is (= :convex-web.command.status/success status))
      (is (= {:convex-web.result/type "Function", 
              :convex-web.result/value "inc"}
            (select-keys result [:convex-web.result/type
                                 :convex-web.result/value])))))
  
  (testing "Lookup doc"
    (let [{::c/keys [status result]}
          (c/execute system {::c/mode :convex-web.command.mode/query
                             ::c/address 9
                             ::c/query
                             {:convex-web.query/source "(doc inc)"
                              :convex-web.query/language :convex-lisp}})]
      
      (is (= :convex-web.command.status/success status))
      (is (= {:convex-web.result/type "Map",
              :convex-web.result/value
              "{:description \"Increments the given number by 1. Converts to Long if necessary.\",:signature [{:return Long,:params [num]}],:type :function,:errors {:CAST \"If the actor argument is not a Number.\"},:examples [{:code \"(inc 10)\"}]}"}
            (select-keys result [:convex-web.result/type
                                 :convex-web.result/value])))))
  
  (testing "Syntax error"
    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/query
                                     {:convex-web.query/source "("
                                      :convex-web.query/language :convex-lisp}})]
      
      (is (= {:convex-web.command/error
              {:message
               "Error while parsing action 'Input/ExpressionList/ZeroOrMore/Sequence/Expression/FirstOf/ExpressionElement/DataStructure/List/FirstOf/Sequence/List_Action1' at input position (line 1, pos 3):\n(\n  ^\n\nconvex.core.exceptions.ParseException: Expected closing ')'"},
              :convex-web.command/status :convex-web.command.status/error}
            
            (select-keys command [::c/status ::c/error])))))
  
  (testing "Cast error"
    (let [{::c/keys [status result error]} 
          (c/execute system {::c/mode :convex-web.command.mode/query
                             ::c/address 9
                             ::c/query
                             {:convex-web.query/source "(map inc 1)"
                              :convex-web.query/language :convex-lisp}})]
      
      (is (= :convex-web.command.status/error status))
      
      (is (= {:code :CAST, 
              :message "Can't convert value of type Long to type Sequence", 
              :trace nil}
            error))
      
      (is (= {:convex-web.result/error-code :CAST,
              :convex-web.result/trace nil,
              :convex-web.result/type "String",
              :convex-web.result/value "Can't convert value of type Long to type Sequence"}
            (select-keys result [:convex-web.result/type
                                 :convex-web.result/value
                                 :convex-web.result/error-code
                                 :convex-web.result/trace]))))))

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