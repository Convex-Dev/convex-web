(ns convex-web.command-test
  (:require [clojure.test :refer :all]
            [convex-web.command :as c]
            [convex-web.convex :as convex])
  (:import (convex.core Init)
           (convex.core.lang Context)
           (convex.core.data Address)))

(def context (Context/createFake Init/INITIAL_STATE))

(deftest object-string-test
  (testing "Default"
    (is (= "{:a 1}\n" (c/object-string (convex/execute context {:a 1})))))

  (testing "Address"
    (is (= (.toChecksumHex (convex/execute context *address*))
           (c/object-string (convex/execute context *address*))))))

(deftest result-metadata-test
  (testing "Nil"
    (is (= {:type :nil}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object :nil}))))

  (testing "Boolean"
    (is (= {:type :boolean}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context true)})))

    (is (= {:type :boolean}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context false)}))))

  (testing "Number"
    (is (= {:type :number}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context 1)})))
    (is (= {:type :number}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context 1.0)}))))

  (testing "String"
    (is (= {:type :string}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context "Hello")}))))

  (testing "Map"
    (is (= {:type :map}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context {})}))))

  (testing "List"
    (is (= {:type :list}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context '(1 2 3))}))))

  (testing "Vector"
    (is (= {:type :vector}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context [1 2 3])}))))

  (testing "Set"
    (is (= {:type :set}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context #{1 2 3})}))))

  (testing "Address"
    (is (= {:type :address}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context *address*)}))))

  (testing "Blob"
    (is (= {:type :blob
            :hex-string (.toHexString ^Address (convex/execute context *address*))
            :length 32}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context (blob *address*))}))))

  (testing "Symbol"
    (is (= {:type :symbol}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context 's)}))))

  (testing "Function"
    (is (= {:type :function
            :doc
            {:description "Increments the given number by 1. Converts to Long if necessary."
             :examples [{:code "(inc 10)"}]
             :signature [{:params ['num]}]
             :symbol "inc"
             :type :function}}
           (c/result-metadata {::c/transaction {:convex-web.transaction/source "inc"}
                               ::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context inc)}))))

  (testing "Macro"
    (is (= {:type :macro
            :doc
            {:description "Defines a function in the current environment."
             :examples [{:code "(defn my-square [x] (* x x))"}]
             :signature [{:params ['name 'params '& 'body]}]
             :symbol "defn"
             :type :macro}}
           (-> (c/result-metadata {::c/transaction {:convex-web.transaction/source "defn"}
                                   ::c/status :convex-web.command.status/success
                                   ::c/object (convex/execute context defn)})
               (select-keys [:type :doc])))))

  (testing "Special"
    (is (= {:type :special
            :doc
            {:description "Creates a definition in the current environment. This value will persist in the enviroment owned by the current account."
             :examples [{:code "(def a 10)"}]
             :signature [{:params ['sym 'value]}]
             :symbol "def"
             :type :special}}
           (-> (c/result-metadata {::c/transaction {:convex-web.transaction/source "def"}
                                   ::c/status :convex-web.command.status/success
                                   ::c/object (convex/execute context defn)})
               (select-keys [:type :doc]))))))

(deftest prune-test
  (is (= {::c/status :convex-web.command.status/running}
         (c/prune {::c/status :convex-web.command.status/running
                   :x 1
                   :y 2})))

  (is (= {::c/status :convex-web.command.status/success
          ::c/object "#{}\n"}
         (c/prune {::c/status :convex-web.command.status/success
                   ::c/object (convex/execute context #{})})))

  (is (= {::c/status :convex-web.command.status/success
          ::c/object "[1 \"Hello\" sym]\n"}
         (c/prune {::c/status :convex-web.command.status/success
                   ::c/object (convex/execute context [1 "Hello" 'sym])})))

  (is (= {::c/status :convex-web.command.status/error
          ::c/error {:code :UNDECLARED :message "x"}}
         (c/prune {::c/status :convex-web.command.status/error
                   ::c/error {:code :UNDECLARED :message "x"}}))))

