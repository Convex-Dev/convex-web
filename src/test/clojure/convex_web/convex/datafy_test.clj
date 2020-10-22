(ns convex-web.convex.datafy-test
  (:require [clojure.test :refer :all]
            [clojure.datafy :refer [datafy]]

            [convex-web.convex.datafy]
            [convex-web.convex :as convex]
            [convex-web.test :refer :all]))

(def context (convex-context))

(deftest datafy-test
  (testing "String"
    (is (= "" (datafy (convex/execute context "")))))

  (testing "Symbol"
    (is (= 'sym (datafy (convex/execute context 'sym)))))

  (testing "Keyword"
    (is (= :k (datafy (convex/execute context :k)))))

  (testing "List"
    (is (= '() (datafy (convex/execute context '())))))

  (testing "Vector"
    (is (= '() (datafy (convex/execute context [])))))

  (testing "Map"
    (is (= {} (datafy (convex/execute context {})))))

  (testing "Set"
    (is (= #{} (datafy (convex/execute context #{})))))

  (testing "Address"
    (is (= #{:checksum-hex :hex-string} (set (keys (datafy (convex/execute context *address*)))))))

  (testing "Blob"
    (is (= #{:hex-string :length} (set (keys (datafy (convex/execute context (blob *address*)))))))))
