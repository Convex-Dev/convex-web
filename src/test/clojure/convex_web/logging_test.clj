(ns convex-web.logging-test
  (:require
   [convex-web.logging :as logging]

   [clojure.test :refer [deftest testing is]]
   [clojure.stacktrace :as stacktrace]))

(deftest labels-test
  (testing "Default"
    (is (= {"eventName" "endpoint"
            "namespace" "convex-web.logging-test"}
           (logging/logging-labels {:mulog/event-name :logging.event/endpoint
                                    :mulog/namespace "convex-web.logging-test"}))))

  (testing "Default"
    (is (= {"eventName" "faucet"
            "namespace" "convex-web.logging-test"}
           (logging/logging-labels {:mulog/event-name :logging.event/faucet
                                    :mulog/namespace "convex-web.logging-test"
                                    :address "ABC"})))))

(deftest json-payload-test
  (testing "Default"
    (is (= {} (.getDataAsMap (logging/logging-json-payload {}))))
    (is (= {"message" "Foo"} (.getDataAsMap (logging/logging-json-payload {:message "Foo"}))))
    (let [ex (ex-info "Foo" {})]
      (is (= {"exception" (with-out-str (stacktrace/print-stack-trace ex))}
             (.getDataAsMap (logging/logging-json-payload {:exception ex}))))))

  (testing "REPL Error"
    (is (= {"source" "x"} (.getDataAsMap
                            (logging/logging-json-payload
                              {:mulog/event-name :logging.event/repl-error
                               :source "x"}))))

    (is (= {"message" "Foo"
            "source" "x"}
           (.getDataAsMap
             (logging/logging-json-payload
               {:mulog/event-name :logging.event/repl-error
                :message "Foo"
                :source "x"}))))))

