(ns convex-web.convex-test
  (:require [clojure.test :refer [deftest is run-tests testing]]

            [convex-web.convex :as convex]
            [convex-web.test :refer [catch-throwable make-convex-context]])
  (:import (convex.core.data Address Blob Syntax Maps Symbol Keyword Vectors)
           (convex.core Init Result)
           (convex.core.lang Core Context)
           (convex.core.data.prim CVMLong)
           (clojure.lang ExceptionInfo)))

(deftest read-source-test
  (is (= [] (convex/read-source "()" :convex-lisp)))
  (is (= [] (convex/read-source "[]" :convex-lisp)))

  (testing "Blob literal"
    (is (= (Blob/fromHex "Fd0cfE9EDf767823b927a25D6840ae2367455c29A7B77CBBF146Be3EeF270356")
           (convex/read-source "0xFd0cfE9EDf767823b927a25D6840ae2367455c29A7B77CBBF146Be3EeF270356" :convex-lisp)))

    (let [e (try
              (convex/read-source "0xABC" :convex-lisp)
              (catch ExceptionInfo ex
                ex))]
      (is (= "Reader error: Parse error at Position{line=1, column=6}\n Source: <>\n Message: null"
             (ex-message e)))
      (is (= {:cognitect.anomalies/category :cognitect.anomalies/incorrect
              :convex-web.result/error-code :READER}
             (ex-data e)))))

  (testing "Address literal"
    (is (= (Address/create 8)
           (convex/read-source "#8" :convex-lisp))))

  (testing "Read all with do"
    (is (= [(Symbol/create "do")
            (CVMLong/create 1)
            (CVMLong/create 2)]
           (convex/read-source "1 2" :convex-lisp)))))

(deftest lookup-metadata-test
  (is (= 
        '{:doc
          {:description
           "Applies a function to each element of a data structure in sequence, and returns a vector of results. Additional collection may be provided to call a function with higher arity.",
           :examples [{:code "(map inc [1 2 3])"}],
           :signature [{:params [f coll]} {:params [f coll1 coll2 & more-colls]}],
           :type :function}}
        (convex/datafy
          (convex/lookup-metadata 
            (convex/hero-fake-context)
            (Symbol/create "map"))))))

(deftest datafy-test
  (let [context (make-convex-context)]
    (testing "nil"
      (is (= nil (convex/datafy nil))))

    (testing "Byte"
      (is (= convex.core.data.prim.CVMByte (type (convex/execute-string context "(nth 0xFF 0)"))))
      (is (= 255 (convex/datafy (convex/execute-string context "(nth 0xFF 0)")))))
    
    (testing "Char"
      (is (= \n (convex/datafy (convex/execute context \n)))))

    (testing "Long"
      (is (= 1 (convex/datafy (convex/execute context 1)))))

    (testing "Double"
      (is (= 1.0 (convex/datafy (convex/execute context 1.0)))))

    (testing "Keyword"
      (is (= :a (convex/datafy (convex/execute context :a)))))

    (testing "Symbol"
      (is (= 's (convex/datafy (convex/execute-string context "'s"))))
      (is (= (symbol "a" "b") (convex/datafy (convex/execute-string context "'a/b"))))
      (is (= (symbol "#8" "inc") (convex/datafy (convex/execute-string context "'#8/inc")))))

    (testing "List"
      (is (= '() (convex/datafy (convex/execute context '())))))

    (testing "Vector"
      (is (= [] (convex/datafy (convex/execute context [])))))

    (testing "Map"
      (is (= {} (convex/datafy (convex/execute context {}))))
      (is (= {'(1) '(2)} (convex/datafy (convex/execute context {(list 1) (list 2)})))))

    (testing "Set"
      (is (= #{} (convex/datafy (convex/execute context #{})))))

    (testing "AccountKey"
      (is (= (-> Init/HERO_KP .getAccountKey .toChecksumHex) (convex/datafy (.getAccountKey Init/HERO_KP)))))

    (testing "Address"
      (is (= (.longValue Init/HERO) (convex/datafy Init/HERO))))

    (testing "Blob"
      (is (= (.toHexString (Blob/create (.getBytes "Text"))) (convex/datafy (Blob/create (.getBytes "Text"))))))

    (testing "Syntax"
      (is (= 1 (convex/datafy (Syntax/create (CVMLong/create 1)))))
      (is (= (Maps/empty) (convex/datafy (Syntax/create (Maps/empty))))))

    (testing "Non-CVM types will throw"
      (is (= "Can't datafy :x clojure.lang.Keyword." (ex-message (catch-throwable (convex/datafy :x)))))
      (is (= "Can't datafy () clojure.lang.PersistentList$EmptyList." (ex-message (catch-throwable (convex/datafy '())))))
      (is (= "Can't datafy 1 java.lang.Long." (ex-message (catch-throwable (convex/datafy 1)))))
      (is (= "Can't datafy 1.0 java.lang.Double." (ex-message (catch-throwable (convex/datafy 1.0)))))
      (is (= "Can't datafy \"ABC\" java.lang.String." (ex-message (catch-throwable (convex/datafy "ABC"))))))))

(deftest address-test
  (testing "Coerce string to Address"
    (is (= (convex/address "1") (Address/create 1)))
    (is (= (convex/address "#1") (Address/create 1))))

  (testing "No coercion"
    (is (= (convex/address (Address/create 1)) (Address/create 1))))

  (testing "Can't coerce nil"
    (is (= "Can't coerce nil to convex.core.data.Address."
           (ex-message (catch-throwable (convex/address nil))))))

  (testing "Can't coerce empty string"
    (is (= "Can't coerce empty string to convex.core.data.Address."
           (ex-message (catch-throwable (convex/address ""))))))

  (testing "Invalid Address string"
    (is (= "Can't coerce \"foo\" to convex.core.data.Address." (ex-message (catch-throwable (convex/address "foo")))))))


(deftest rollback-test
  (testing "Rollback exceptional value"
    (let [context1 (make-convex-context)
          
          context2 (convex/execute-string* context1 "(def x 1)")
          
          context3 (convex/execute-string* context2 "(do (def x 2) (rollback :abort))")]
      
      (is (= (Keyword/create "abort") (.getValue (.getExceptional context3)))))))

(deftest kind-test
  (let [context (make-convex-context)]
    (testing "Boolean"
      (is (= :boolean (convex/value-kind (convex/execute context true)))))

    (testing "Long"
      (is (= :long (convex/value-kind (convex/execute context 1)))))
    
    (testing "Byte"
      (is (= :byte (convex/value-kind (convex/execute-string context "(nth 0xFF 0)")))))

    (testing "Double"
      (is (= :double (convex/value-kind (convex/execute context 1.0)))))

    (testing "String"
      (is (= :string (convex/value-kind (convex/execute context "")))))

    (testing "Symbol"
      (is (= :symbol (convex/value-kind (convex/execute context 'sym)))))

    (testing "Special"
      (is (= :symbol (convex/value-kind (convex/execute-string context "def")))))

    (testing "Map"
      (is (= :map (convex/value-kind (convex/execute context {})))))

    (testing "List"
      (is (= :list (convex/value-kind (convex/execute context '())))))

    (testing "Vector"
      (is (= :vector (convex/value-kind (convex/execute context [])))))

    (testing "Set"
      (is (= :set (convex/value-kind (convex/execute context #{})))))

    (testing "Address"
      (is (= :address (convex/value-kind (convex/execute context *address*)))))

    (testing "Blob"
      (is (= :blob (convex/value-kind (convex/execute context (blob *address*))))))

    (testing "Unknown"
      (is (= nil (convex/value-kind (Syntax/create (Maps/empty)))))
      (is (= nil (convex/value-kind (convex/execute-string context "abc")))))))


(deftest result-data-test
  (testing "Long"
    (is (= 
          {:convex-web.result/id 1,
           :convex-web.result/type "Long",
           :convex-web.result/value "1",
           :convex-web.result/value-kind :long}
          (convex/result-data 
            (Result/create
              (CVMLong/create 1) 
              (CVMLong/create 1))))))
  
  (testing "Keyword"
    (is (= 
          {:convex-web.result/id 1,
           :convex-web.result/type "Keyword",
           :convex-web.result/value ":a",
           :convex-web.result/value-kind :keyword}
          (convex/result-data 
            (Result/create
              (CVMLong/create 1) 
              (Keyword/create "a"))))))
  
  (testing "Address"
    (is (= 
          {:convex-web.result/id 1,
           :convex-web.result/type "Address",
           :convex-web.result/value "#1",
           :convex-web.result/value-kind :address}
          (convex/result-data 
            (Result/create
              (CVMLong/create 1) 
              (Address/create 1))))))
  
  (testing "Vector"
    (is (= 
          {:convex-web.result/id 1,
           :convex-web.result/type "Vector",
           :convex-web.result/value "[]",
           :convex-web.result/value-kind :vector}
          (convex/result-data 
            (Result/create
              (CVMLong/create 1) 
              (Vectors/empty))))))
  
  (testing "Map"
    (is (= 
          {:convex-web.result/id 1,
           :convex-web.result/type "Map",
           :convex-web.result/value "{}",
           :convex-web.result/value-kind :map}
          (convex/result-data 
            (Result/create
              (CVMLong/create 1) 
              (Maps/empty))))))
  
  (testing "Core function"
    (is (= 
          {:convex-web.result/id 1,
           :convex-web.result/type "Function",
           :convex-web.result/value "map",
           :convex-web.result/value-kind :function}
          (convex/result-data 
            (Result/create
              (CVMLong/create 1) 
              Core/MAP))))))


(comment
  (require 'convex-web.convex-test)
  (in-ns 'convex-web.convex-test)
  
  (run-tests))