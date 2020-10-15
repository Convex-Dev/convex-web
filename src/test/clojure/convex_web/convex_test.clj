(ns convex-web.convex-test
  (:require [clojure.test :refer :all]

            [convex-web.convex :as convex]
            [convex-web.test :refer :all])
  (:import (convex.core.data Address Maps)
           (convex.core.lang Context)
           (convex.core Init)))

(use-fixtures :once (spec-fixture))

(def context (Context/createFake Init/STATE))

(deftest datafy-test
  (testing "Char"
    (is (instance? java.lang.Character (convex/datafy (convex/execute context \n)))))

  (testing "String"
    (is (instance? java.lang.String (convex/datafy (convex/execute context "String")))))

  (testing "Long"
    (is (instance? java.lang.Long (convex/datafy (convex/execute context 1)))))

  (testing "Double"
    (is (instance? Double (convex/datafy (convex/execute context 1.0)))))

  (testing "Keyword"
    (is (instance? clojure.lang.Keyword (convex/datafy (convex/execute context :a)))))

  (testing "Symbol"
    (is (instance? clojure.lang.Symbol (convex/datafy (convex/execute context 's))))
    (is (instance? clojure.lang.Symbol (convex/datafy (convex/execute context 'a/b)))))

  (testing "List"
    (is (instance? clojure.lang.ISeq (convex/datafy (convex/execute context '())))))

  (testing "Vector"
    (is (instance? clojure.lang.IPersistentVector (convex/datafy (convex/execute context [])))))

  (testing "Map"
    (is (instance? clojure.lang.IPersistentMap (convex/datafy (convex/execute context {})))))

  (testing "Set"
    (is (instance? clojure.lang.IPersistentSet (convex/datafy (convex/execute context #{})))))

  (testing "Custom types"
    (let [address (Address/fromHex "D0F65BB5d87316D6b7d74dbb93da3D7E416f8B0aF8FffbBD1f276A15f4907bfE")]
      (is (= {"0xD0F65BB5d87316D6b7d74dbb93da3D7E416f8B0aF8FffbBD1f276A15f4907bfE" 1}
             (convex/datafy (Maps/create address 1))))

      (is (= {"D0F65BB5d87316D6b7d74dbb93da3D7E416f8B0aF8FffbBD1f276A15f4907bfE" 1}
             (convex/datafy (Maps/create address 1) {:default
                                                     (fn [x]
                                                       (condp instance? x
                                                         Address (.toChecksumHex ^Address x)

                                                         nil))}))))))

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
    (is (= :boolean (convex/kind (convex/execute context true)))))

  (testing "Number"
    (is (= :number (convex/kind (convex/execute context 1))))
    (is (= :number (convex/kind (convex/execute context 1.0)))))

  (testing "String"
    (is (= :string (convex/kind (convex/execute context "")))))

  (testing "Symbol"
    (is (= :symbol (convex/kind (convex/execute context 'sym)))))

  (testing "Map"
    (is (= :map (convex/kind (convex/execute context {})))))

  (testing "List"
    (is (= :list (convex/kind (convex/execute context '())))))

  (testing "Vector"
    (is (= :vector (convex/kind (convex/execute context [])))))

  (testing "Set"
    (is (= :set (convex/kind (convex/execute context #{})))))

  (testing "Address"
    (is (= :address (convex/kind (convex/execute context *address*)))))

  (testing "Blob"
    (is (= :blob (convex/kind (convex/execute context (blob *address*)))))))

(deftest sequence-number-test
  (binding [convex/sequence-number-ref (atom {})]
    (let [addr (convex/address "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f")]
      (is (= {addr 0} (convex/set-sequence-number! {:address addr
                                                    :not-found 0})))))

  (binding [convex/sequence-number-ref (atom {(convex/address "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f") 1})]
    (let [addr (convex/address "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f")]
      (is (= {addr 1} (convex/set-sequence-number! {:address addr})))))

  (binding [convex/sequence-number-ref (atom {})]
    (let [addr (convex/address "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f")]
      (is (= {addr 1} (convex/set-sequence-number! {:address addr
                                                    :next 1}))))))
