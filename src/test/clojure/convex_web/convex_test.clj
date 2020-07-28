(ns convex-web.convex-test
  (:require [clojure.test :refer :all]
            [convex-web.convex :as convex])
  (:import (convex.core.data Keyword Symbol Address Vectors Maps)))

(deftest con->clj-test
  (testing "Keyword"
    (is (keyword? (convex/con->clj (Keyword/create "a")))))

  (testing "Symbol"
    (is (symbol? (convex/con->clj (Symbol/create "a"))))
    (is (symbol? (convex/con->clj (Symbol/createWithNamespace "f" "core")))))

  (testing "Sequence"
    (is (sequential? (convex/con->clj (Vectors/create (into-array [1 2 3]))))))

  (testing "HashMap"
    (is (map? (convex/con->clj (Maps/empty))))))

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
