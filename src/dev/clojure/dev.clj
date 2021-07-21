(ns dev
  (:require 
   [convex-web.system :as system]
   [convex-web.component :as component]
   [convex-web.convex :as convex]
   [convex-web.session :as session]
   [convex-web.account :as account]
   [convex-web.web-server :as web-server]
   [convex-web.client :as client]
   [convex-web.store :as store]
   [convex-web.config :as config]
   
   [clojure.java.io :as io]
   [clojure.stacktrace :as stacktrace]
   [clojure.tools.logging :as log]
   
   [com.stuartsierra.component.repl :refer [set-init reset stop system]]
   [aero.core :as aero]
   [ring.mock.request :as mock]
   [datalevin.core :as d])
  
  (:import 
   (etch EtchStore)
   (convex.peer Server API)
   (convex.core Peer)
   (convex.core.init Init AInitConfig)
   (convex.core.lang Core Reader Context)
   (convex.core.crypto AKeyPair PFXTools)
   (convex.core.data Keywords Address Hash AccountKey ASet AHashMap Symbol AccountStatus)))

;; -- Logging
(set-init
  (fn [_]
    (component/system :dev)))


;; FIXME
(def context 
  nil
  #_(Context/createFake 
    (Init/createState (AInitConfig/create)) Init/RESERVED_ADDRESS))

(defn ^Peer peer []
  (system/convex-peer system))

(defmacro execute [form]
  `(convex/execute context ~form))

(defn execute-string [source]
  (convex/execute-string context source))

(defn db []
  @(system/db-conn system))


(comment
  
  (config/read-config :dev)
  
  (reset)
  
  (stop) 
  
  ;; -- Bootstrap Peer
  
  ;; Address for convex.world
  (convex/server-peer-controller (system/convex-server system))
    
  
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
  
  
  ;; Globals is a tuple with timestamp + fees + juice price.
  (.getGlobals (convex/consensus-state (peer)))
  
  
  (str Init/HERO)
  (str Init/VILLAIN)
  
  
  (convex/consensus-point (convex/peer-order (peer)))
  
  
  ;; -- Execute  
  
  (execute-string "(nth 0xFF 0)")
  
  (instance? convex.core.lang.AFn (execute-string "inc"))
  (instance? convex.core.lang.impl.Fn (execute-string "inc"))
  (instance? convex.core.lang.impl.Fn (execute-string "(fn [x] x)")) 
  
  
  ;; Library metadata.
  (convex/library-metadata context "convex.trust")
  
  
  ;; `convex.core.lang.impl.Fn/getParams` returns AVector<Syntax>
  (def params (.getParams (execute-string "(fn [x y] (+ x y))")))
  
  ;; Parameters data.
  (map
    (fn [param]
      ;; A function parameter Syntax object wraps a Symbol:
      ;; 
      ;; (.getValue param) ;; => convex.core.data.Symbol
      ;; 
      ;; Symbol is ASymbolic, so it's possibe to read its name `(.getName symbol)`.
      {:symbol (-> param .getValue .getName .toString)
       :metadata (.getMeta param)})
    params)
  
  
  ;; Lookup metadata.
  (convex/datafy 
    (or 
      (.lookupMeta context Init/HERO (Symbol/create "map"))
      (.lookupMeta context (Symbol/create "map"))))
  
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
  Core/METADATA
  
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