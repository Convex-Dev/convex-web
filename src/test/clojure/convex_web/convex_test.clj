(ns convex-web.convex-test
  (:require [clojure.test :refer :all]

            [convex-web.system :as sys]
            [convex-web.convex :as convex]
            [convex-web.test :refer :all]
            [clojure.spec.alpha :as s])
  (:import (convex.core.data Address Blob Syntax Maps)
           (convex.core Init)))

(def system nil)

(def context (convex-context))

(deftest datafy-test
  (let [context (convex-context)]
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
      (is (= 's (convex/datafy (convex/execute context 's))))
      (is (= 'a/b (convex/datafy (convex/execute context 'a/b)))))

    (testing "List"
      (is (= '() (convex/datafy (convex/execute context '())))))

    (testing "Vector"
      (is (= [] (convex/datafy (convex/execute context [])))))

    (testing "Map"
      (is (= {} (convex/datafy (convex/execute context {})))))

    (testing "Set"
      (is (= #{} (convex/datafy (convex/execute context #{})))))

    (testing "Address"
      (is (= (.toString Init/HERO) (convex/datafy Init/HERO))))

    (testing "Blob"
      (is (= (.toHexString (Blob/create (.getBytes "Text"))) (convex/datafy (Blob/create (.getBytes "Text"))))))

    (testing "Expander"
      (is (string? (convex/datafy (convex/execute-string context "defn")))))

    (testing "Syntax"
      (is (= 1 (convex/datafy (Syntax/create 1))))
      (is (= (Maps/empty) (convex/datafy (Syntax/create (Maps/empty))))))

    (testing "Can't datafy"
      (testing "java.lang.String"
        (is (= "Can't datafy java.lang.String." (.getMessage (catch-throwable (convex/datafy "String"))))))

      (testing "convex.core.lang.impl.ErrorValue"
        (is (= "Can't datafy convex.core.lang.impl.ErrorValue."
               (.getMessage (catch-throwable (convex/datafy (convex/execute-string context "(map inc 1)"))))))))))



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
  (testing "Boolean"
    (is (= :boolean (convex/value-kind (convex/execute context true)))))

  (testing "Number"
    (is (= :number (convex/value-kind (convex/execute context 1))))
    (is (= :number (convex/value-kind (convex/execute context 1.0)))))

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
    (is (= nil (convex/value-kind (convex/execute-string context "abc"))))))



