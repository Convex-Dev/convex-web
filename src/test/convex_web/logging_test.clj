(ns convex-web.logging-test
  (:require [clojure.test :refer :all]
            [convex-web.logging :as logging]))

(deftest labels-test
  (testing "Default"
    (is (= {"eventName" ":logging.event/endpoint"
            "namespace" "convex-web.logging-test"
            "session" "Session"}
           (logging/labels {:mulog/event-name :logging.event/endpoint
                            :mulog/namespace "convex-web.logging-test"
                            :context {:ring-session "Session"}}))))

  (testing "Default"
    (is (= {"eventName" ":logging.event/faucet"
            "namespace" "convex-web.logging-test"}
           (logging/labels {:mulog/event-name :logging.event/faucet
                            :mulog/namespace "convex-web.logging-test"
                            :address "ABC"})))))

(deftest json-payload-test
  (testing "Default"
    (is (= {} (.getDataAsMap (logging/json-payload {}))))
    (is (= {"message" "Foo"} (.getDataAsMap (logging/json-payload {:message "Foo"}))))
    (let [ex (ex-info "Foo" {})]
      (is (= {"exception_stack_trace"
              (with-out-str (.printStackTrace ex))}
             (.getDataAsMap (logging/json-payload {:exception ex}))))))

  (testing "REPL Error"
    (is (= {"source" "x"} (.getDataAsMap
                            (logging/json-payload
                              {:mulog/event-name :logging.event/repl-error
                               :source "x"}))))

    (is (= {"message" "Foo"
            "source" "x"}
           (.getDataAsMap
             (logging/json-payload
               {:mulog/event-name :logging.event/repl-error
                :message "Foo"
                :source "x"}))))))

