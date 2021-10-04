(ns convex-web.site.sandbox.hiccup-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer [deftest is testing]]

   [convex-web.site.sandbox.hiccup :as hiccup]))

(deftest hiccup-element
  (testing "Text with string"

    (testing "Text without attributes"
      (is (= {:tag :text
              :content [[:terminal [:string "Hello"]]]}

            (s/conform ::hiccup/element [:text "Hello"]))))

    (testing "Text with attributes"
      (is (= {:tag :text
              :attributes {}
              :content [[:terminal [:number 1]]]}

            (s/conform ::hiccup/element [:text {} 1])))))

  (testing "Text with number"
    (is (= {:tag :text
            :attributes {}
            :content [[:terminal [:number 1]]]}

          (s/conform ::hiccup/element [:text {} 1]))))

  (testing "Text with nested element"
    (is (= {:tag :text
            :attributes {},
            :content
            [[:element
              {:tag :text
               :attributes {},
               :content
               [[:terminal [:string "Hello"]]]}]],}

          (s/conform ::hiccup/element [:text {} [:text {} "Hello"]])))))
