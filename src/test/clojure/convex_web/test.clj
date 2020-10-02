(ns convex-web.test
  (:require [clojure.spec.test.alpha :as stest]
            [com.stuartsierra.component]
            [clojure.spec.alpha :as s]))

(defmacro with-try [& body]
  `(try
     ~@body
     (catch Exception ex#
       ex#)))

(defn spec-fixture []
  (fn [f]
    (s/check-asserts true)
    (stest/instrument)

    (f)

    (s/check-asserts false)
    (stest/unstrument)))


(defn system-fixture [system-var]
  (fn [f]
    (let [system (com.stuartsierra.component/start
                   (convex-web.component/system :test))]

      (alter-var-root system-var (constantly system))

      (f)

      (com.stuartsierra.component/stop system))))
