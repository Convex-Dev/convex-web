(ns convex-web.convex-test
  (:require [clojure.test :refer :all]

            [convex-web.convex :as convex]
            [convex-web.test :refer :all])
  (:import (convex.core.data Address Blob Syntax Maps Symbol)
           (convex.core Init)
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
      (is (= #:cognitect.anomalies{:category :cognitect.anomalies/incorrect}
             (ex-data e)))))

  (testing "Address literal"
    (is (= (Address/create 8)
           (convex/read-source "#8" :convex-lisp))))

  (testing "Read all with do"
    (is (= [(Symbol/create "do")
            (CVMLong/create 1)
            (CVMLong/create 2)]
           (convex/read-source "1 2" :convex-lisp)))))

(deftest datafy-test
  (let [context (make-convex-context)]
    (testing "nil"
      (is (= nil (convex/datafy nil))))

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
      (is (= {} (convex/datafy (convex/execute context {})))))

    (testing "Set"
      (is (= #{} (convex/datafy (convex/execute context #{})))))

    (testing "AccountKey"
      (is (= (-> Init/HERO_KP .getAccountKey .toChecksumHex) (convex/datafy (.getAccountKey Init/HERO_KP)))))

    (testing "Address"
      (is (= (.longValue Init/HERO) (convex/datafy Init/HERO))))

    (testing "Blob"
      (is (= (.toHexString (Blob/create (.getBytes "Text"))) (convex/datafy (Blob/create (.getBytes "Text"))))))

    (testing "Expander"
      (is (string? (convex/datafy (convex/execute-string context "defn")))))

    (testing "Syntax"
      (is (= 1 (convex/datafy (Syntax/create 1))))
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


(deftest kind-test
  (let [context (make-convex-context)]
    (testing "Boolean"
      (is (= :boolean (convex/value-kind (convex/execute context true)))))

    (testing "Long"
      (is (= :long (convex/value-kind (convex/execute context 1)))))

    (testing "Double"
      (is (= :double (convex/value-kind (convex/execute context 1.0)))))

    (testing "String"
      (is (= :string (convex/value-kind (convex/execute context "")))))

    (testing "Symbol"
      (is (= :symbol (convex/value-kind (convex/execute context 'sym)))))

    (testing "Macro"
      (is (= :macro (convex/value-kind (convex/execute-string context "defn")))))

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


(comment
  (require 'convex-web.convex-test)
  (in-ns 'convex-web.convex-test)
  
  (run-tests))