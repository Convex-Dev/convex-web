(ns convex-web.test
  (:require [convex-web.component]

            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]

            [com.stuartsierra.component]
            [clojure.java.io :as io]))

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

      ;; Reset the database
      (doseq [f (reverse (file-seq (io/file (get-in system [:config :config :datalevin :dir]))))]
        (io/delete-file f))

      (f)

      (com.stuartsierra.component/stop system))))
