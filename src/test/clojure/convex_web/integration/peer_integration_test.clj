(ns convex-web.integration.peer-integration-test
  (:require [clojure.test :refer :all]

            [convex-web.system :as sys]
            [convex-web.convex :as convex]
            [convex-web.test :refer :all]
            [clojure.spec.alpha :as s])
  (:import (convex.core Init)))

(def system nil)

(use-fixtures :once (join-fixtures [(make-system-fixture #'system) spec-fixture]))

(deftest account-status-test
  (testing "Account Status"
    (is (= true (some? (convex/account-status (sys/convex-peer system) Init/HERO))))))

(deftest result-data-test
  (testing "Inc 1"
    (let [result (-> (sys/convex-client system)
                     (convex/query {:address Init/HERO :source "(inc 1)" :lang :convex-lisp})
                     (convex/result-data))]

      (testing "Expected keys"
        (is (= #{:convex-web.result/id
                 :convex-web.result/value
                 :convex-web.result/value-kind}
               (-> result keys set))))

      (testing "Expected values"
        (is (= #:convex-web.result{:value 2
                                   :value-kind :long}
               (select-keys result [:convex-web.result/value
                                    :convex-web.result/value-kind]))))))

  (testing "Error code"
    (let [result (-> (sys/convex-client system)
                     (convex/query {:address Init/HERO :source "(map inc 1)" :lang :convex-lisp})
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
                                   :value "Can't convert 1 of class convex.core.data.prim.CVMLong to class class convex.core.data.ASequence"
                                   :value-kind :string}
               (select-keys result [:convex-web.result/value
                                    :convex-web.result/value-kind
                                    :convex-web.result/error-code
                                    :convex-web.result/trace])))))))
