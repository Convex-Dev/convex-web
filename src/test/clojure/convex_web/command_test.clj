(ns convex-web.command-test
  (:require [clojure.test :refer :all]
            [convex-web.command :as c]
            [convex-web.convex :as convex])
  (:import (convex.core Init)
           (convex.core.lang Context)))

(def context (Context/createFake Init/INITIAL_STATE))

(deftest wrap-result-test
  (testing "Address"
    (is (= (let [a (convex/execute context *address*)]
             {::c/id 1
              ::c/mode :convex-web.command.mode/query
              ::c/query {:convex-web.query/source "*address*"
                         :convex-web.query/language :convex-lisp}
              ::c/status :convex-web.command.status/success
              ::c/object
              {:checksum-hex (.toChecksumHex a)
               :hex-string (.toHexString a)}})
           (c/wrap-result {::c/id 1
                           ::c/mode :convex-web.command.mode/query
                           ::c/query {:convex-web.query/source "*address*"
                                      :convex-web.query/language :convex-lisp}
                           ::c/status :convex-web.command.status/success
                           ::c/object (convex/execute context *address*)}))))

  (testing "Blob"
    (is (= (let [a (convex/execute context *address*)]
             {::c/id 1
              ::c/mode :convex-web.command.mode/query
              ::c/query {:convex-web.query/source "(blob *address*)"
                         :convex-web.query/language :convex-lisp}
              ::c/status :convex-web.command.status/success
              ::c/object
              {:hex-string (.toHexString a)
               :length 32}})
           (c/wrap-result {::c/id 1
                           ::c/mode :convex-web.command.mode/query
                           ::c/query {:convex-web.query/source "(blob *address*)"
                                      :convex-web.query/language :convex-lisp}
                           ::c/status :convex-web.command.status/success
                           ::c/object (convex/execute context (blob *address*))}))))

  (testing "Default"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "{:a 1}"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object {:a 1}}
           (c/wrap-result {::c/id 1
                           ::c/mode :convex-web.command.mode/query
                           ::c/query {:convex-web.query/source "{:a 1}"
                                      :convex-web.query/language :convex-lisp}
                           ::c/status :convex-web.command.status/success
                           ::c/object (convex/execute context {:a 1})})))

    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "map"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object "map"}
           (c/wrap-result {::c/id 1
                           ::c/mode :convex-web.command.mode/query
                           ::c/query {:convex-web.query/source "map"
                                      :convex-web.query/language :convex-lisp}
                           ::c/status :convex-web.command.status/success
                           ::c/object (convex/execute context map)})))))

(deftest wrap-result-metadata-test
  (testing "Nil"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "nil"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object :nil
            ::c/metadata {:type :nil}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "nil"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object :nil}))))

  (testing "Boolean"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "true"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object true
            ::c/metadata {:type :boolean}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "true"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context true)})))

    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "false"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object false
            ::c/metadata {:type :boolean}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "false"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context false)}))))

  (testing "Number"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "1"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object 1
            ::c/metadata {:type :number}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "1"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context 1)})))
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "1.0"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object 1.0
            ::c/metadata {:type :number}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "1.0"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context 1.0)}))))

  (testing "String"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "\"Hello\""
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object "Hello"
            ::c/metadata {:type :string}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "\"Hello\""
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context "Hello")}))))

  (testing "Map"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "{}"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object {}
            ::c/metadata {:type :map}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "{}"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context {})}))))

  (testing "List"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "'(1 2 3)"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object '(1 2 3)
            ::c/metadata {:type :list}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "'(1 2 3)"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context '(1 2 3))}))))

  (testing "Vector"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "[1 2 3]"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object [1 2 3]
            ::c/metadata {:type :vector}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "[1 2 3]"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context [1 2 3])}))))

  (testing "Set"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "#{}"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object #{}
            ::c/metadata {:type :set}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "#{}"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context #{})}))))

  (testing "Address"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "*address*"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object (convex/execute context *address*)
            ::c/metadata {:type :address}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "*address*"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context *address*)}))))

  (testing "Blob"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "(blob *address*)"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object (convex/execute context (blob *address*))
            ::c/metadata {:type :blob}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "(blob *address*)"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context (blob *address*))}))))

  (testing "Symbol"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "'s"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object (convex/execute context 's)
            ::c/metadata {:type :symbol}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "'s"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context 's)}))))

  (testing "Function"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "inc"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object (convex/execute context inc)
            ::c/metadata
            {:type :function
             :doc
             {:description "Increments the given number by 1. Converts to Long if necessary."
              :examples [{:code "(inc 10)"}]
              :signature [{:params ['num]}]
              :symbol "inc"
              :type :function}}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "inc"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/success
                                    ::c/object (convex/execute context inc)}))))

  (testing "Macro"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "defn"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object (convex/execute context defn)
            ::c/metadata
            {:type :macro
             :doc
             {:description "Defines a function in the current environment."
              :examples [{:code "(defn my-square [x] (* x x))"}]
              :signature [{:params ['name 'params '& 'body]}]
              :symbol "defn"
              :type :macro}}}
           (-> (c/wrap-result-metadata {::c/id 1
                                        ::c/mode :convex-web.command.mode/query
                                        ::c/query {:convex-web.query/source "defn"
                                                   :convex-web.query/language :convex-lisp}
                                        ::c/status :convex-web.command.status/success
                                        ::c/object (convex/execute context defn)})
               (update ::c/metadata dissoc :source)
               (update ::c/metadata dissoc :start)
               (update ::c/metadata dissoc :end)))))

  (testing "Special"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "def"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/success
            ::c/object (convex/execute context defn)
            ::c/metadata
            {:type :special
             :doc
             {:description "Creates a definition in the current environment. This value will persist in the enviroment owned by the current account."
              :examples [{:code "(def a 10)"}]
              :signature [{:params ['sym 'value]}]
              :symbol "def"
              :type :special}}}
           (-> (c/wrap-result-metadata {::c/id 1
                                        ::c/mode :convex-web.command.mode/query
                                        ::c/query {:convex-web.query/source "def"
                                                   :convex-web.query/language :convex-lisp}
                                        ::c/status :convex-web.command.status/success
                                        ::c/object (convex/execute context defn)})
               (update ::c/metadata dissoc :source)
               (update ::c/metadata dissoc :start)
               (update ::c/metadata dissoc :end)))))

  (testing "Error"
    (is (= {::c/id 1
            ::c/mode :convex-web.command.mode/query
            ::c/query {:convex-web.query/source "Noop"
                       :convex-web.query/language :convex-lisp}
            ::c/status :convex-web.command.status/error
            ::c/metadata {:type :error}
            ::c/error {:message "Error"}}
           (c/wrap-result-metadata {::c/id 1
                                    ::c/mode :convex-web.command.mode/query
                                    ::c/query {:convex-web.query/source "Noop"
                                               :convex-web.query/language :convex-lisp}
                                    ::c/status :convex-web.command.status/error
                                    ::c/error {:message "Error"}})))))



