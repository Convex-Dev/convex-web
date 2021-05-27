(ns dev
  (:require [convex-web.system :as system]
            [convex-web.component :as component]
            [convex-web.convex :as convex]
            [convex-web.session :as session]
            [convex-web.account :as account]
            [convex-web.web-server :as web-server]
            [convex-web.client :as client]

            [clojure.java.io :as io]
            [clojure.stacktrace :as stacktrace]

            [com.stuartsierra.component.repl :refer [set-init reset system]]
            [ring.mock.request :as mock]
            [datalevin.core :as d])
  (:import (convex.core Init Peer)
           (convex.core.lang Core Reader Context)
           (convex.core.crypto Hash AKeyPair)
           (convex.core.data AccountKey)))

;; -- Logging
(set-init
  (fn [_]
    (component/system :dev)))


(def context (Context/createFake (Init/createState) Init/HERO))

(defn ^Peer peer []
  (system/convex-peer system))

(defmacro execute [form]
  `(convex/execute context ~form))

(defn execute-string [source]
  (convex/execute-string context source))

(defn db []
  @(system/db-conn system))


(comment
  
  (reset)
  
  
  ;;; -- Create Account
  (let [^AKeyPair generated-key-pair (AKeyPair/generate)
        ^AccountKey account-key (.getAccountKey generated-key-pair)
        ^String account-public-key (.toChecksumHex account-key)]
    (convex/create-account (system/convex-client system) account-public-key))
  
  
  ;; -- Reset database
  (let [dir (get-in system [:config :config :datalevin :dir])]
    (doseq [f (reverse (file-seq (io/file dir)))]
      (io/delete-file f))
    
    (println "Deleted db" dir))
  
  
  ;; -- Testing
  (let [handler (web-server/site system)]
    (handler (mock/request :post "/api/internal/generate-account")))
  
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
  
  
  ;; All Convex Addresses.
  (def all-accounts (convex/accounts-indexed (system/convex-peer system)))
  
  (get all-accounts (.longValue Init/HERO))
  
  (map
    (fn [[address-long _]]
      address-long)
    all-accounts)
  
  (convex/account-status (system/convex-peer system) Init/HERO)
  
  
  (let [a (.getAccounts (.getConsensusState (system/convex-peer system)))]
    (doseq [[k v] (convex.core.lang.RT/sequence a)]
      (print k v)))
  
  
  (str Init/HERO)
  (str Init/VILLAIN)
  
  
  (convex/consensus-point (convex/peer-order (peer)))
  
  
  ;; -- Execute  
  
  (instance? convex.core.lang.AFn (execute-string "inc"))
  (instance? convex.core.lang.impl.Fn (execute-string "inc"))
  (instance? convex.core.lang.impl.Fn (execute-string "(fn [x] x)"))
  
  (def params (.getParams (execute-string "(fn [x y] (+ x y))")))
  
  (def param-x (first params))
  
  ;; Parameters data.
  (map
    (fn [param]
      {:symbol (-> param .getValue .getName .toString)
       :metadata (.getMeta param)})
    params)
  
  
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
  
  (account/find-all (db))
  (account/find-by-address (db) 48)
  
  
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