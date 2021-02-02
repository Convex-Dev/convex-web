(ns convex-web.command-test
  (:require [clojure.test :refer :all]
            [convex-web.specs]
            [convex-web.command :as c]
            [convex-web.convex :as convex]
            [convex-web.test :refer :all])
  (:import (convex.core.data StringShort)
           (convex.core.lang Core)))

(def context (make-convex-context))

(def system nil)

(use-fixtures :each (make-system-fixture #'system))

(deftest query-mode-test
  (testing "Simple Commands"
    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/address 9
                                     ::c/query
                                     {:convex-web.query/source "1"
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/success
              ::c/object 1}
             (select-keys command [::c/status ::c/object]))))

    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/address 9
                                     ::c/query
                                     {:convex-web.query/source "\"Hello\""
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/success
              ::c/object "Hello"}
             (select-keys command [::c/status ::c/object])))))

  (testing "Symbol lookup"
    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/address 9
                                     ::c/query
                                     {:convex-web.query/source "inc"
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/success
              ::c/object Core/INC}
             (select-keys command [::c/status ::c/object])))))

  (testing "Lookup doc"
    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/address 9
                                     ::c/query
                                     {:convex-web.query/source "(doc inc)"
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/success
              ::c/object
              {:description "Increments the given number by 1. Converts to Long if necessary."
               :examples [{:code "(inc 10)"}]
               :signature [{:params ['num]}]
               :type :function}}
             (select-keys command [::c/status ::c/object])))))

  (testing "Syntax error"
    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/query
                                     {:convex-web.query/source "("
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/error
              ::c/error {:message "Syntax error."}}
             (select-keys command [::c/status ::c/object ::c/error])))))

  (testing "Cast error"
    (let [command (c/execute system {::c/mode :convex-web.command.mode/query
                                     ::c/address 9
                                     ::c/query
                                     {:convex-web.query/source "(map inc 1)"
                                      :convex-web.query/language :convex-lisp}})]

      (is (= {::c/status :convex-web.command.status/error
              ::c/object "Can't convert 1 of class convex.core.data.prim.CVMLong to class class convex.core.data.ASequence"
              ::c/error
              {:code :CAST
               :message "Can't convert 1 of class convex.core.data.prim.CVMLong to class class convex.core.data.ASequence"
               :trace nil}}
             (select-keys command [::c/status ::c/object ::c/error]))))))

(deftest sandbox-result-test
  (testing "Address"
    (is (= {:address 9}
           (c/sandbox-result (convex/execute context *address*)))))

  (testing "Blob"
    (is (= {:hex-string "0000000000000009"
            :length 8}
           (c/sandbox-result (convex/execute context (blob *address*))))))

  (testing "StringShort"
    ;; Defaults to datafy.
    (is (= "X" (c/sandbox-result (StringShort/create "X"))))))

(deftest result-metadata-test
  (testing "Nil"
    (is (= {} (c/result-metadata (convex/execute context nil)))))

  (testing "Boolean"
    (is (= {:type :boolean} (c/result-metadata (convex/execute context true))))
    (is (= {:type :boolean} (c/result-metadata (convex/execute context false)))))

  (testing "Number"
    (is (= {:type :number} (c/result-metadata (convex/execute context 1))))
    (is (= {:type :number} (c/result-metadata (convex/execute context 1.0)))))

  (testing "String"
    (is (= {:type :string} (c/result-metadata (convex/execute context "Hello")))))

  (testing "Map"
    (is (= {:type :map} (c/result-metadata (convex/execute context {}))))
    (is (= {:type :map} (c/result-metadata (convex/execute context {:a 1})))))

  (testing "List"
    (is (= {:type :list} (c/result-metadata (convex/execute context '(1 2 3))))))

  (testing "Vector"
    (is (= {:type :vector} (c/result-metadata (convex/execute context [1 2 3])))))

  (testing "Set"
    (is (= {:type :set} (c/result-metadata (convex/execute context #{})))))

  (testing "Address"
    (is (= {:type :address} (c/result-metadata (convex/execute context *address*)))))

  (testing "Blob"
    (is (= {:type :blob} (c/result-metadata (convex/execute context (blob *address*))))))

  (testing "Symbol"
    (is (= {:type :symbol} (c/result-metadata (convex/execute context 's)))))

  (testing "Function"
    (is (= {} (c/result-metadata (convex/execute context inc))))
    (is (= {:type :function
            :doc
            {:description "Increments the given number by 1. Converts to Long if necessary."
             :examples [{:code "(inc 10)"}]
             :signature [{:params ['num]}]
             :symbol "inc"
             :type :function}} (c/result-metadata (convex/execute context inc) {:source "inc" :lang :convex-lisp}))))

  (testing "Macro"
    (is (= {} (c/result-metadata (convex/execute context defn))))
    (is (= {:type :macro
            :doc
            {:description "Defines a function in the current environment."
             :examples [{:code "(defn my-square [x] (* x x))"}]
             :signature [{:params ['name 'params '& 'body]}]
             :symbol "defn"
             :type :macro}
            :start 1088} (c/result-metadata (convex/execute context defn) {:source "defn" :lang :convex-lisp}))))

  (testing "Special"
    (is (= {:type :symbol} (c/result-metadata (convex/execute context def))))
    (is (= {:doc {:description "Creates a definition in the current environment. This value will persist in the environment owned by the current account."
                  :examples [{:code "(def a 10)"}]
                  :signature [{:params ['sym 'value]}]
                  :symbol "def"
                  :type :special}
            :type :special}
           (c/result-metadata (convex/execute context def) {:source "def" :lang :convex-lisp})))))



