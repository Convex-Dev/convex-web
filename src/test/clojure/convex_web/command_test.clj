(ns convex-web.command-test
  (:require 
   [clojure.test :refer [deftest is testing use-fixtures]]
   
   [ring.mock.request :as mock]
   
   [convex-web.specs]
   [convex-web.command :as c]
   [convex-web.convex :as convex]
   [convex-web.system :as sys]
   [convex-web.encoding :as encoding]
   [convex-web.web-server :as web-server]
   [convex-web.account :as account]
   [convex-web.test :refer [make-system-fixture]])
  (:import (convex.core.data StringShort)))

(def system nil)

(use-fixtures :each (make-system-fixture #'system))

(deftest transact-mode-test
  (testing "Rollback"
    
    (let [handler (web-server/site system)
          
          {generate-account-body :body} (handler (mock/request :post "/api/internal/generate-account"))
          
          {generated-address :convex-web.account/address} (encoding/transit-decode-string generate-account-body)
          
          _ (handler (mock/request :post "/api/internal/confirm-account"
                       (encoding/transit-encode generated-address)))

          db (sys/db system)

          signer (account/find-by-address db generated-address)
          
          command1 (c/execute system #:convex-web.command {:id (java.util.UUID/randomUUID)
                                                           :timestamp 1
                                                           :mode :convex-web.command.mode/transaction
                                                           :signer signer
                                                           :transaction
                                                           #:convex-web.transaction
                                                           {:source "(def x 1)"
                                                            :type :convex-web.transaction.type/invoke
                                                            :language :convex-lisp}})
          
          command2 (c/execute system #:convex-web.command {:id (java.util.UUID/randomUUID)
                                                           :timestamp 2
                                                           :mode :convex-web.command.mode/transaction
                                                           :signer signer
                                                           :transaction
                                                           #:convex-web.transaction
                                                           {:source "(do (def x 2) (rollback :abort))"
                                                            :type :convex-web.transaction.type/invoke
                                                            :language :convex-lisp}})
          
          command3 (c/execute system #:convex-web.command {:id (java.util.UUID/randomUUID)
                                                           :timestamp 3
                                                           :mode :convex-web.command.mode/transaction
                                                           :signer signer
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
          (c/execute system {::c/id (java.util.UUID/randomUUID)
                             ::c/timestamp 1
                             ::c/mode :convex-web.command.mode/query
                             ::c/signer {:convex-web.account/address 9}
                             ::c/query
                             {:convex-web.query/source "1"
                              :convex-web.query/language :convex-lisp}})]
      
      (is (= :convex-web.command.status/success status))
      (is (= {:convex-web.result/type "Long", 
              :convex-web.result/value "1"}
            (select-keys result [:convex-web.result/type
                                 :convex-web.result/value]))))
    
    (let [{::c/keys [status result]}
          (c/execute system {::c/id (java.util.UUID/randomUUID)
                             ::c/timestamp 1
                             ::c/mode :convex-web.command.mode/query
                             ::c/signer {:convex-web.account/address 9}
                             ::c/query
                             {:convex-web.query/source "1.0"
                              :convex-web.query/language :convex-lisp}})]
      
      (is (= :convex-web.command.status/success status))
      (is (= {:convex-web.result/type "Double", 
              :convex-web.result/value "1.0"}
            (select-keys result [:convex-web.result/type
                                 :convex-web.result/value]))))
    
    (let [{::c/keys [status result]}
          (c/execute system {::c/id (java.util.UUID/randomUUID)
                             ::c/timestamp 1
                             ::c/mode :convex-web.command.mode/query
                             ::c/signer {:convex-web.account/address 9}
                             ::c/query
                             {:convex-web.query/source "\"Hello\""
                              :convex-web.query/language :convex-lisp}})]
      
      (is (= :convex-web.command.status/success status))
      (is (= {:convex-web.result/type "String", 
              :convex-web.result/value "\"Hello\""}
            (select-keys result [:convex-web.result/type
                                 :convex-web.result/value])))))
  
  (testing "Symbol lookup"
    (let [{::c/keys [status result]}
          (c/execute system {::c/id (java.util.UUID/randomUUID)
                             ::c/timestamp 1
                             ::c/mode :convex-web.command.mode/query
                             ::c/signer {:convex-web.account/address 9}
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
          (c/execute system {::c/id (java.util.UUID/randomUUID)
                             ::c/timestamp 1
                             ::c/mode :convex-web.command.mode/query
                             ::c/signer {:convex-web.account/address 9}
                             ::c/query
                             {:convex-web.query/source "(doc inc)"
                              :convex-web.query/language :convex-lisp}})]
      
      (is (= :convex-web.command.status/success status))
      (is (= {:convex-web.result/type "Map",
              :convex-web.result/value "{:description \"Increments the given Long value by 1.\",:signature [{:return Long,:params [num]}],:errors {:CAST \"If the actor argument is not castable to Long.\"},:examples [{:code \"(inc 10)\"}]}"}
            (select-keys result [:convex-web.result/type
                                 :convex-web.result/value])))))
  
  (testing "Syntax error"
    (let [command (c/execute system {::c/id (java.util.UUID/randomUUID)
                                     ::c/timestamp 1
                                     ::c/mode :convex-web.command.mode/query
                                     ::c/query
                                     {:convex-web.query/source "("
                                      :convex-web.query/language :convex-lisp}})]
      
      (is (= {:convex-web.command/status :convex-web.command.status/error
              :convex-web.command/error {:code :READER, :message "Reader error: -1..-1 <missing ')'>"}}
            
            (select-keys command [::c/status ::c/error])))))
  
  (testing "Cast error"
    (let [{::c/keys [status result error]} 
          (c/execute system {::c/id (java.util.UUID/randomUUID)
                             ::c/timestamp 1
                             ::c/mode :convex-web.command.mode/query
                             ::c/signer {:convex-web.account/address 9}
                             ::c/query
                             {:convex-web.query/source "(map inc 1)"
                              :convex-web.query/language :convex-lisp}})]
      
      (is (= :convex-web.command.status/error status))
      
      (is (= {:code :CAST,
              :message "Can't convert value of type Long to type Sequence",
              :trace ["In function: map"]}
            error))
      
      (is (= {:convex-web.result/error-code :CAST,
              :convex-web.result/trace ["In function: map"],
              :convex-web.result/type "String",
              :convex-web.result/value "\"Can't convert value of type Long to type Sequence\""}
            (select-keys result [:convex-web.result/type
                                 :convex-web.result/value
                                 :convex-web.result/error-code
                                 :convex-web.result/trace]))))))

(deftest sandbox-result-test
  (let [context (sys/convex-world-context system)]
    (testing "Long"
      (is (= 1 (c/sandbox-result (convex/execute-string context "1")))))
    
    (testing "Double"
      (is (= 1.0 (c/sandbox-result (convex/execute-string context "1.0")))))
    
    (testing "Address"
      (is (= {:address 12}
            (c/sandbox-result (convex/execute-string context "*address*")))))
    
    (testing "Blob"
      (is (= {:hex-string "000000000000000c"
              :length 8}
            (c/sandbox-result (convex/execute-string context "(blob *address*)"))))))
  
  (testing "StringShort"
    ;; Defaults to datafy.
    (is (= "X" (c/sandbox-result (StringShort/create "X"))))))

(deftest result-metadata-test
  (let [context (sys/convex-world-context system)]
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
      (is (= '{:doc
               {:description "Increments the given Long value by 1.",
                :errors {:CAST "If the actor argument is not castable to Long."},
                :examples [{:code "(inc 10)"}],
                :signature [{:params [num], :return Long}],
                :symbol "inc"},
               :type nil
               :static true}
            (c/result-metadata (convex/execute context inc) {:source "inc" :lang :convex-lisp}))))
    
    (testing "Macro"
      (is (= {} (c/result-metadata (convex/execute context defn))))
      (is (= '{:doc
               {:description "Defines a function in the current environment.",
                :examples [{:code "(defn my-square [x] (* x x))"}],
                :signature [{:params [name params & body]} {:params [name & fn-decls]}],
                :symbol "defn"},
               :expander? true,
               :type nil}
            (c/result-metadata (convex/execute context defn) {:source "defn" :lang :convex-lisp}))))
    
    (testing "Special"
      (is (= {:type :symbol} (c/result-metadata (convex/execute context def))))
      (is (= '{:doc
               {:description
                ["Creates a definition in the current environment. This value will persist in the environment owned by the current account."
                 "The name argument must be a symbol, or a Symbol wrapped in a syntax object with optional metadata."],
                :errors
                {:CAST "If the argument is neither a valid symbol name nor a syntax containing a symbol value."},
                :examples [{:code "(def a 10)"}],
                :signature [{:params [name value]}],
                :symbol "def"},
               :special? true,
               :type nil}
            (c/result-metadata (convex/execute context def) {:source "def" :lang :convex-lisp}))))))
