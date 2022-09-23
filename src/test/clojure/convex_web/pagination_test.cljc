(ns convex-web.pagination-test
  (:require [clojure.test :refer :all]

            [convex.web.pagination :as $.web.pagination]))

(deftest decrease-range-test
  (is (= {:end 15} ($.web.pagination/decrease-range 0)))
  (is (= {:end 15} ($.web.pagination/decrease-range 10)))
  (is (= {:end 15} ($.web.pagination/decrease-range 15)))
  (is (= {:start 5 :end 20} ($.web.pagination/decrease-range 20)))
  (is (= {:start 15 :end 30} ($.web.pagination/decrease-range 30))))

(deftest increase-range-test
  (is (= {:end 10} ($.web.pagination/increase-range 5 10)))
  (is (= {:end 20 :start 5} ($.web.pagination/increase-range 5 30))))

(deftest page-count-test
  (is (= 1 ($.web.pagination/page-count 0)))
  (is (= 1 ($.web.pagination/page-count 10)))
  (is (= 1 ($.web.pagination/page-count 15)))
  (is (= 2 ($.web.pagination/page-count 16)))
  (is (= 2 ($.web.pagination/page-count 30)))
  (is (= 3 ($.web.pagination/page-count 35))))

(deftest page-num-reverse-test
  (is (= 1 ($.web.pagination/page-num-reverse 35 35)))
  (is (= 1 ($.web.pagination/page-num-reverse 30 35)))
  (is (= 1 ($.web.pagination/page-num-reverse 20 35)))
  (is (= 2 ($.web.pagination/page-num-reverse 5 35)))
  (is (= 3 ($.web.pagination/page-num-reverse 4 35)))
  (is (= 3 ($.web.pagination/page-num-reverse 0 35))))

(deftest page-num-test
  (let [n 10]
    (is (= 1 ($.web.pagination/page-num -100 n)))
    (is (= 1 ($.web.pagination/page-num -1 n)))
    (is (= 1 ($.web.pagination/page-num 0 n)))
    (is (= 1 ($.web.pagination/page-num 1 n)))
    (is (= 1 ($.web.pagination/page-num 9 n)))
    (is (= 1 ($.web.pagination/page-num 10 n)))
    (is (= 2 ($.web.pagination/page-num 11 n)))
    (is (= 2 ($.web.pagination/page-num 20 n)))
    (is (= 3 ($.web.pagination/page-num 21 n)))
    (is (= 3 ($.web.pagination/page-num 30 n)))))
