(ns convex-web.convex-test
  (:require [clojure.test :refer :all]

            [convex-web.system :as sys]
            [convex-web.convex :as convex]
            [convex-web.test :refer :all])
  (:import (convex.core.data Address Blob Syntax Maps Amount)
           (convex.core Init)))

(def system nil)

(def context (convex-context))

(use-fixtures :once (join-fixtures [(make-system-fixture #'system) spec-fixture]))

(deftest datafy-test
  (testing "nil"
    (is (= nil (convex/datafy nil))))

  (testing "Char"
    (is (= \n (convex/datafy (convex/execute context \n)))))

  (testing "Long"
    (is (= 1 (convex/datafy (convex/execute context 1)))))

  (testing "Amount"
    (is (= 1 (convex/datafy (Amount/create 1)))))

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
             (.getMessage (catch-throwable (convex/datafy (convex/execute-string context "(map inc 1)")))))))))

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

(deftest result-data-test
  (testing "Inc 1"
    (let [result (-> (sys/convex-client system)
                     (convex/query {:source "(inc 1)" :lang :convex-lisp})
                     (convex/result-data))]

      (testing "Expected keys"
        (is (= #{:convex-web.result/id
                 :convex-web.result/value
                 :convex-web.result/value-kind}
               (-> result keys set))))

      (testing "Expected values"
        (is (= #:convex-web.result{:value 2
                                   :value-kind :number}
               (select-keys result [:convex-web.result/value
                                    :convex-web.result/value-kind]))))))

  (testing "Error code"
    (let [result (-> (sys/convex-client system)
                     (convex/query {:source "(map inc 1)" :lang :convex-lisp})
                     (convex/result-data))]

      (testing "Expected keys"
        (is (= #{:convex-web.result/error-code
                 :convex-web.result/id
                 :convex-web.result/trace
                 :convex-web.result/value
                 :convex-web.result/value-kind}
               (-> result keys set))))

      (testing "Expected values"
        (is (= #:convex-web.result{:error-code :CAST
                                   :trace nil
                                   :value "Can't convert 1 of class java.lang.Long to class class convex.core.data.ASequence"
                                   :value-kind :string}
               (select-keys result [:convex-web.result/value
                                    :convex-web.result/value-kind
                                    :convex-web.result/error-code
                                    :convex-web.result/trace])))))))

