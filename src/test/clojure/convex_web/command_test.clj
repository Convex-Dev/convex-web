(ns convex-web.command-test
  (:require [clojure.test :refer :all]
            [convex-web.command :as c]
            [convex-web.convex :as convex])
  (:import (convex.core Init)
           (convex.core.lang Context)
           (convex.core.data Address)))

(def context (Context/createFake Init/INITIAL_STATE))

(deftest result-metadata-test
  (testing "Nil"
    (is (= {:type :nil
            :doc {:type :nil}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object :nil}))))

  (testing "Boolean"
    (is (= {:type :boolean
            :doc {:type :boolean}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context true)})))

    (is (= {:type :boolean
            :doc {:type :boolean}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context false)}))))

  (testing "Number"
    (is (= {:type :number
            :doc {:type :number}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context 1)})))
    (is (= {:type :number
            :doc {:type :number}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context 1.0)}))))

  (testing "String"
    (is (= {:type :string
            :doc {:type :string}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context "Hello")}))))

  (testing "Map"
    (is (= {:type :map
            :doc {:type :map}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context {})}))))

  (testing "List"
    (is (= {:type :list
            :doc {:type :list}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context '(1 2 3))}))))

  (testing "Vector"
    (is (= {:type :vector
            :doc {:type :vector}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context [1 2 3])}))))

  (testing "Set"
    (is (= {:type :set
            :doc {:type :set}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context #{1 2 3})}))))

  (testing "Address"
    (is (= {:type :address
            :doc {:type :address}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context *address*)}))))

  (testing "Blob"
    (is (= {:doc {:type :blob}
            :type :blob
            :hex-string (.toHexString ^Address (convex/execute context *address*))
            :length 32}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context (blob *address*))}))))

  (testing "Symbol"
    (is (= {:type :symbol
            :doc {:type :symbol}}
           (c/result-metadata {::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context 's)}))))

  (testing "Function"
    (is (= {:doc
            {:description "Increments the given number by 1. Converts to Long if necessary."
             :examples [{:code "(inc 10)"}]
             :signature [{:params ['num]}]
             :symbol "inc"
             :type :function}}
           (c/result-metadata {::c/transaction {:convex-web.transaction/source "inc"}
                               ::c/status :convex-web.command.status/success
                               ::c/object (convex/execute context inc)}))))

  (testing "Macro"
    (is (= {:doc
            {:description "Defines a function in the current environment."
             :examples [{:code "(defn my-square [x] (* x x))"}]
             :signature [{:params ['name 'params '& 'body]}]
             :symbol "defn"
             :type :macro}}
           (-> (c/result-metadata {::c/transaction {:convex-web.transaction/source "defn"}
                                   ::c/status :convex-web.command.status/success
                                   ::c/object (convex/execute context defn)})
               (select-keys [:doc])))))

  (testing "Special"
    (is (= {:doc
            {:description "Creates a definition in the current environment. This value will persist in the enviroment owned by the current account."
             :examples [{:code "(def a 10)"}]
             :signature [{:params ['sym 'value]}]
             :symbol "def"
             :type :special}}
           (-> (c/result-metadata {::c/transaction {:convex-web.transaction/source "def"}
                                   ::c/status :convex-web.command.status/success
                                   ::c/object (convex/execute context defn)})
               (select-keys [:doc]))))))

