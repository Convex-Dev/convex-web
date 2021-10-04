(ns convex-web.site.sandbox.hiccup-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer [deftest is testing]]

   [convex-web.site.sandbox.hiccup :as hiccup]))

(deftest hiccup-element-test
  (testing "String and number are not valid elements"
    (is (= :clojure.spec.alpha/invalid (s/conform ::hiccup/element 1)))
    (is (= :clojure.spec.alpha/invalid (s/conform ::hiccup/element "Hello"))))

  (testing "Empty vector is not a valid element"
    (is (= :clojure.spec.alpha/invalid (s/conform ::hiccup/element []))))

  (testing "Text without attributes"
    (is (= {:tag :text
            :content [[:string "Hello"]]}

          (s/conform ::hiccup/element [:text "Hello"]))))

  (testing "Text with attributes"
    (is (= {:tag :text
            :attributes {}
            :content [[:number 1]]}

          (s/conform ::hiccup/element [:text {} 1])))

    (testing "Text with number"
      (is (= {:tag :text
              :attributes {}
              :content [[:number 1]]}

            (s/conform ::hiccup/element [:text {} 1])))))

  (testing "Text with nested element"
    (is (= {:tag :text
            :attributes {},
            :content
            [[:element
              {:tag :text
               :attributes {},
               :content
               [[:string "Hello"]]}]]}

          (s/conform ::hiccup/element [:text {} [:text {} "Hello"]])))))

(deftest compile-test
  (is  (= [:span "1"] (hiccup/compile [:text 1])))
  (is  (= [:span "1"] (hiccup/compile [:text {} 1])))

  (is  (= [:span "Hello"] (hiccup/compile [:text "Hello"])))
  (is  (= [:span "\"Hello\""] (hiccup/compile [:text "\"Hello\""])))

  (is  (= [:span "{:tag :text, :content [[:number 1]]}"]
         (hiccup/compile [:text {} [:text 1]]))))
