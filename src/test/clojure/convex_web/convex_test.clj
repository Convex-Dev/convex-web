(ns convex-web.convex-test
  (:require [clojure.test :refer :all]
            [convex-web.convex :as convex])
  (:import (convex.core.data Keyword Symbol Address Vectors Maps Lists Sets)
           (convex.core.lang Context)
           (convex.core Init)))

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
      (is (= {"0xd0f65bb5d87316d6b7d74dbb93da3d7e416f8b0af8fffbbd1f276a15f4907bfe" 1}
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
           (try
             (convex/address nil)
             (catch Exception ex
               (ex-message ex))))))

  (testing "Can't coerce empty string"
    (is (= "Can't coerce empty string to convex.core.data.Address."
           (try
             (convex/address "")
             (catch Exception ex
               (ex-message ex))))))

  (testing "Invalid Address String"
    (is (= "Invalid Address hex String [foo]" (try
                                                (convex/address "foo")
                                                (catch Error ex
                                                  (ex-message ex))))))

  (testing "Coerce string to Address"
    (let [address-string "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f"]
      (is (convex/address address-string))
      (is (= (convex/address address-string)
             (Address/fromHex address-string))))))
