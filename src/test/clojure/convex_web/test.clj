(ns convex-web.test
  (:require [clojure.spec.test.alpha :as stest]))

(defmacro with-try [& body]
  `(try
     ~@body
     (catch Exception ex#
       ex#)))

(defn spec-instrument-fixture [f]
  (stest/instrument)

  (f)

  (stest/unstrument))
