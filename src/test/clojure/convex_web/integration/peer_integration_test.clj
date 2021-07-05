(ns convex-web.integration.peer-integration-test
  (:require [clojure.test :refer :all]

            [convex-web.system :as sys]
            [convex-web.convex :as convex]
            [convex-web.test :refer :all]
            [clojure.spec.alpha :as s])
  (:import (convex.core.init Init)))

(def system nil)

(use-fixtures :once (join-fixtures [(make-system-fixture #'system) spec-fixture]))

(deftest account-status-test
  (testing "Account Status"
    (is (some? 
          (convex/account-status 
            (sys/convex-peer system)
            (convex/key-pair-data-address 
              (convex/convex-world-key-pair-data)))))))

(deftest result-data-test
  (testing "Inc 1"
    (let [result (-> (sys/convex-client system)
                   (convex/query {:address
                                  (convex/key-pair-data-address 
                                    (convex/convex-world-key-pair-data))
                                  
                                  :source "(inc 1)" 
                                  
                                  :lang :convex-lisp})
                   (convex/result-data))]
      
      (testing "Expected keys"
        (is (= #{:convex-web.result/id
                 :convex-web.result/value
                 :convex-web.result/type}
              (-> result keys set))))
      
      (testing "Expected values"
        (is (= #:convex-web.result{:value "2"}
              (select-keys result [:convex-web.result/value
                                   :convex-web.result/value-kind]))))))
  
  (testing "Error code"
    (let [result (-> (sys/convex-client system)
                   (convex/query {:address 
                                  (convex/key-pair-data-address 
                                    (convex/convex-world-key-pair-data))
                                  
                                  :source "(map inc 1)" 
                                  
                                  :lang :convex-lisp})
                   (convex/result-data))]
      
      (testing "Expected keys"
        (is (= #{:convex-web.result/error-code
                 :convex-web.result/id
                 :convex-web.result/type
                 :convex-web.result/trace
                 :convex-web.result/value}
              (-> result keys set))))
      
      (testing "Expected values"
        (is (= #:convex-web.result{:error-code :CAST
                                   :trace nil
                                   :value "Can't convert value of type Long to type Sequence"}
              (select-keys result [:convex-web.result/value
                                   :convex-web.result/value-kind
                                   :convex-web.result/error-code
                                   :convex-web.result/trace])))))))
