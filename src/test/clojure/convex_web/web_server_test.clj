(ns convex-web.web-server-test
  (:require [clojure.test :refer [deftest is testing]]
            
            [convex-web.web-server :as web-server]))

(deftest json-key-fn-test
  (is (= "1" (web-server/json-key-fn 1)))
  (is (= "Foo"  (web-server/json-key-fn "Foo")))
  (is (= "[]"  (web-server/json-key-fn [])))
  (is (= "#{}" (web-server/json-key-fn #{})))
  (is (= "()" (web-server/json-key-fn '())))
  (is (= "()" (web-server/json-key-fn  (map identity [])))))

(deftest json-encode-test
  (testing "Collections"
    (is (= "[]"  (web-server/json-encode [])))
    (is (= "[]" (web-server/json-encode #{})))
    (is (= "[]" (web-server/json-encode '())))
    (is (= "[]" (web-server/json-encode  (map identity [])))))
  
  (testing "Object keys"
    (is (= "{\"1\":1}"  (web-server/json-encode {1 1})))
    (is (= "{\"()\":[]}"  (web-server/json-encode {'() '()})))
    (is (= "{\"[]\":[]}"  (web-server/json-encode {[] '()})))
    (is (= "{\"#{}\":[]}"  (web-server/json-encode {#{} '()})))
    (is (= "{\"{}\":[]}"  (web-server/json-encode {{} '()})))
    (is (= "{\"()\":[]}"  (web-server/json-encode {(map identity []) []})))))
  