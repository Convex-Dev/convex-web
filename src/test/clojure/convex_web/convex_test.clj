(ns convex-web.convex-test
  (:require [clojure.test :refer :all]

            [convex-web.convex :as convex]
            [convex-web.test :refer :all])
  (:import (convex.core.data Address Blob)
           (convex.core Init)))

(use-fixtures :once (spec-fixture))

(def context (convex-context))

(deftest datafy-test
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
    (is (= (.toChecksumHex Init/HERO) (convex/datafy Init/HERO))))

  (testing "Blob"
    (is (= (.toHexString (Blob/create (.getBytes "Text"))) (convex/datafy (Blob/create (.getBytes "Text"))))))

  (testing "Can't datafy"
    (testing "java.lang.String"
      (is (= "Can't datafy java.lang.String." (.getMessage (catch-throwable (convex/datafy "String"))))))))

(deftest address-test
  (testing "Can't coerce nil"
    (is (= "Can't coerce nil to convex.core.data.Address."
           (ex-message (catch-throwable (convex/address nil))))))

  (testing "Can't coerce empty string"
    (is (= "Can't coerce empty string to convex.core.data.Address."
           (ex-message (catch-throwable (convex/address ""))))))

  (testing "Invalid Address String"
    (is (= "Invalid Address hex String [foo]" (ex-message (catch-throwable (convex/address "foo"))))))

  (testing "Coerce string to Address"
    (let [address-string "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f"]
      (is (convex/address address-string))
      (is (= (convex/address address-string)
             (Address/fromHex address-string))))))


(deftest key-pair-test
  (let [hero-key-pair (-> Init/HERO_KP convex/key-pair-data convex/create-key-pair)]
    (is (= (.getAddress Init/HERO_KP) (.getAddress hero-key-pair)))
    #_(is (= (.toHexString (.getEncodedPrivateKey Init/HERO_KP)) (.toHexString (.getEncodedPrivateKey hero-key-pair))))))


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
    (is (= :blob (convex/value-kind (convex/execute context (blob *address*)))))))

