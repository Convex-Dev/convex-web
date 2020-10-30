(ns dev
  (:require [convex-web.system :as system]
            [convex-web.peer :as peer]
            [convex-web.component :as component]
            [convex-web.convex :as convex]
            [convex-web.session :as session]
            [convex-web.account :as account]
            [convex-web.logging :as logging]
            [convex-web.web-server :as web-server]
            [convex-web.command :as command]
            [convex-web.client :as client]
            [convex-web.web-server :as web-server]

            [clojure.test :refer [is]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.repl :refer [doc]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.stacktrace :as stacktrace]
            [clojure.datafy :refer [datafy]]
            [clojure.tools.logging :as log]

            [com.stuartsierra.component.repl :refer [set-init reset system]]
            [kaocha.repl :as kaocha]
            [ring.mock.request :as mock]
            [aero.core :as aero]
            [datalevin.core :as d]
            [nano-id.core :as nano-id]
            [org.httpkit.client :as http]
            [expound.alpha :as expound])
  (:import (convex.core Init Peer)
           (convex.core.lang Core Reader Context)
           (convex.core.crypto Hash)))

;; -- Logging
(set-init
  (fn [_]
    (component/system :dev)))

(def context (Context/createFake Init/STATE Init/HERO))

(defn ^Peer peer []
  (system/convex-peer-server system))

(defmacro execute [form]
  `(convex/execute context ~form))

(defn execute-string [source]
  (convex/execute-string context source))

(defn db []
  @(system/db-conn system))

(comment

  ;; -- Reset database
  (let [dir (get-in system [:config :config :datalevin :dir])]
    (doseq [f (reverse (file-seq (io/file dir)))]
      (io/delete-file f))

    (println "Deleted db" dir))


  ;; -- Testing
  (let [handler (web-server/site system)]
    (handler (mock/request :post "/api/internal/generate-account")))


  (clojure.test/run-tests
    'convex-web.specs-test
    'convex-web.internal-api-test)

  (kaocha/test-plan)
  (kaocha/run :unit)


  ;; -- Sessions
  (d/q '[:find [(pull ?e [*
                          {:convex-web.session/accounts
                           [:convex-web.account/address]}]) ...]
         :in $
         :where [?e :convex-web.session/id _]]
       @(system/db-conn system))


  (d/q '[:find (pull ?e [{:convex-web.session/accounts
                          [:convex-web.account/address]}]) .
         :in $ ?id
         :where [?e :convex-web.session/id ?id]]
       @(system/db-conn system) "mydbOh9wCdTcF_vLvUVHR")

  (session/find-session @(system/db-conn system) "iGlF3AZWw0eGuGfL_ib4-")


  (let [a (.getAccounts (.getConsensusState (system/convex-peer-server system)))]
    (doseq [[k v] (convex.core.lang.RT/sequence a)]
      (print k v)))


  (str Init/HERO)
  (str Init/VILLAIN)


  (convex/consensus-point (convex/peer-order (peer)))


  ;; -- Execute

  (execute nil)
  (execute 1)
  (execute \h)
  (execute "Hello")
  (execute (map inc [1 2 3]))
  (execute x)

  (execute-string "(fn [x] x)")

  (convex/execute-scrypt context "def x = 1;")
  (convex/execute-scrypt context "do { inc(1); }")

  (convex/execute-scrypt context "when (true) {}")
  (convex/execute-scrypt context "when (true) { 1; }")
  (convex/execute-scrypt context "if (true) 1;")
  (convex/execute-scrypt context "if (true) { 1; 2; }")
  (convex/execute-scrypt context "if (true) 1; else 2;")
  (convex/execute-scrypt context "if (false) 1; else 2;")
  (convex/execute-scrypt context "do { def x? = true; if (x?) { 1; 2; } 1; }")
  (convex/execute-scrypt context "{ def f = fn (x, y) { map(inc, [x, y]); }; f(1, 2); }")
  (convex/execute-scrypt context "map(fn(x){ inc(x); }, [1, 2])")


  (try
    (Reader/read "(]")
    (catch Throwable e
      (println (.getMessage (stacktrace/root-cause e)))))


  ;; --

  (def status (convex/account-status (peer) "3333333333333333333333333333333333333333333333333333333333333333"))

  (convex/environment-data status)

  (convex/account-status-data *1)

  (account/find-all (db))
  (account/find-by-address (db) "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f")


  ;; -- Session

  (session/all-ring (db))

  (session/all (db))
  (session/find-session (db) "4feac0cd-cc06-4a3b-bcad-54596771356b")
  (session/find-account (db) "f7d696Fc1884ed5A7294A4F765206DB32dCDbCAB35C84DF7a8348bc2bF3b8f45")

  ;; --


  Core/ENVIRONMENT

  (convex/core-metadata)
  (convex/convex-core-reference)


  ;; ----------------------

  (def prepared
    (->> (range 10)
         (map
           (fn [n]
             (client/POST-public-v1-transaction-prepare'
               "http://localhost:8080"
               {:address (.toChecksumHex Init/HERO)
                :source (str n)})))
         (sort-by :sequence_number)))


  (doseq [{:keys [hash]} prepared]
    (client/POST-public-v1-transaction-submit'
      "http://localhost:8080"
      {:address (.toChecksumHex Init/HERO)
       :hash hash
       :sig (.toHexString (.sign Init/HERO_KP (Hash/fromHex hash)))}))

  @(client/POST-public-v1-transaction-submit
     "http://localhost:8080"
     {:address (.toChecksumHex Init/HERO)
      :hash "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b"
      :sig (client/sig Init/HERO_KP "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b")})


  ;; ----------------------


  ;; Hash
  ;; => 4fd279dd67a506bbd987899293d1a4d763f6da04941ccc4748f8dcf548e68bb7

  (client/POST-public-v1-transaction-submit'
    "http://localhost:8080"
    {:address (.toChecksumHex Init/HERO)
     :hash "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b"
     :sig (client/sig Init/HERO_KP "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b")})

  @(client/POST-v1-faucet
     "http://localhost:8080"
     {:address "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71"
      :amount 500})

  )