(ns convex-web.test
  (:require [convex-web.component]
            [convex-web.encoding :as encoding]

            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]

            [ring.mock.request :as mock]
            [com.stuartsierra.component]))

(defmacro catch-throwable [& body]
  `(try
     ~@body
     (catch Throwable ex#
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

(defn transit-body [request body]
  (-> request
      (mock/content-type "application/transit+json")
      (mock/body (encoding/transit-encode body))))