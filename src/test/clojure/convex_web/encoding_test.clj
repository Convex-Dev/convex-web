(ns convex-web.encoding-test
  (:require [clojure.test :refer :all]

            [convex-web.encoding :as encoding])
  (:import (convex.core Init)))

(deftest encode-test
  (is (= (.toString Init/HERO) (encoding/transit-decode-string (encoding/transit-encode Init/HERO)))))
