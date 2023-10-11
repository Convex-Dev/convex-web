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

   [clojure.test :as test]
   [clojure.java.io :as io]
   [clojure.stacktrace :as stacktrace]
   [clojure.string :as str]
   [clojure.data.json :as json]
   
   [com.stuartsierra.component.repl :refer [set-init reset stop system]]
   [aero.core :as aero]
   [ring.mock.request :as mock]
   [datalevin.core :as d]
   [org.httpkit.client :as http])
  
  (:import 
   (etch EtchStore)
   (convex.peer Server API)
   (convex.core Peer)
   (convex.core.init Init)
   (convex.core.lang Core Reader Context)
   (convex.core.crypto AKeyPair PFXTools)
   (convex.core.data Blob Hash AccountKey Symbol)))

;; -- Logging
(set-init
  (fn [_]
    (component/system :dev)))


(defn convex-world-context []
  (system/convex-world-context system))

(defn peer ^Peer []
  (system/convex-peer system))

(defn db []
  @(system/db-conn system))


(comment
  
  (config/read-config :dev)
  
  (reset)
  
  (stop) 


  (def ctx (convex-world-context))


  (test/run-all-tests #"convex-web.*")

  
  ;; -- Bootstrap Peer
  
  ;; Address for convex.world
  (convex/server-peer-controller (system/convex-server system))
  
  
  ;; -- Reset database
  (let [dir (get-in system [:config :config :datalevin :dir])]
    (doseq [f (reverse (file-seq (io/file dir)))]
      (io/delete-file f))
    
    (println "Deleted db" dir))

  ;; Generate a new key pair.
  (convex/key-pair-data (convex/generate-key-pair))
  
  (convex/key-pair-data
    (AKeyPair/create
      (Blob/fromHex "e7fc701f56bb8b602aeb6b96980038c3ad7419b578ee91ccac06ba6a21ec5259")))

  ;; Recreate a KeyPair from a Seed:
  (AKeyPair/create (Blob/fromHex (-> (convex/generate-key-pair) .getSeed .toHexString)))


  ;; -- Testing
  (let [handler (web-server/site system)]
    (handler (mock/request :post "/api/internal/generate-account")))
  
  
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
  
  (convex/execute-string ctx "(nth 0xFF 0)")

  (type (convex/execute-string ctx "if"))
  ;; => convex.core.lang.impl.Fn

  (type (convex/execute-string ctx "loop"))
  ;; => convex.core.data.Symbol

  (type (convex/execute-string ctx "inc"))
  ;; => convex.core.lang.Core$75

  (= true (instance? convex.core.lang.AFn (convex/execute-string ctx "inc")))
  (= true (instance? convex.core.lang.impl.CoreFn (convex/execute-string ctx "inc")))

  (= true (instance? convex.core.lang.AFn (convex/execute-string ctx "defn")))
  (= false (instance? convex.core.lang.impl.CoreFn (convex/execute-string ctx "defn")))

  (= true (instance? convex.core.lang.AFn (convex/execute-string ctx "(fn [x] x)")))
  (= false (instance? convex.core.lang.impl.CoreFn (convex/execute-string ctx "(fn [x] x)")))

  
  ;; Library metadata.
  (convex/library-metadata (system/convex-world-context system) "convex.trust")
  
  
  ;; `convex.core.lang.impl.Fn/getParams` returns AVector<Syntax>
  (def params (.getParams (convex/execute-string ctx "(fn [x y] (+ x y))")))
  
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
      (.lookupMeta ctx Init/HERO (Symbol/create "map"))
      (.lookupMeta ctx (Symbol/create "map"))))
  
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
  (session/find-session (db) "ee9e0671-1c8e-4787-a60c-a97543ef634a")
  (session/find-account (db) {:sid "ee9e0671-1c8e-4787-a60c-a97543ef634a" :address 87})
  
  ;; --
  
  
  Core/ENVIRONMENT
  Core/METADATA
  
  (convex/core-metadata)
  (convex/convex-core-reference)
  
  
  ;; ----------------------

  (convex/key-pair-data (convex/generate-key-pair))

  
  (dotimes [_ 500]
    (let [^AKeyPair generated-key-pair (AKeyPair/generate)
          
          ^AccountKey account-key (.getAccountKey generated-key-pair)
          
          ^String account-public-key (.toChecksumHex account-key)]
      
      (client/POST-public-v1-createAccount "https://convex.world" account-public-key)))
  
  
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
  
  
  (def md (slurp (io/resource "markdown/glossary.md")))
  
  (def md-split-and-clean (remove str/blank? (str/split md #"##\s")))
  
  
  (map #(re-find #"^\w+" %) md-split-and-clean)
  (map #(str/split % #"^\w*\n*") md-split-and-clean)
  
  
  )


(comment

  ;; HubSpot Form

  (def response
    (http/post "https://api.hsforms.com/submissions/v3/integration/submit/24109496/bc3f0027-bc36-41d6-bfdb-c19700419d20"
      {:headers {"Content-Type" "application/json"}
       :body
       (json/write-str
         {:fields
          [{:objectTypeId "0-1"
            :name "firstname"
            :value "Test"}

           {:objectTypeId "0-1"
            :name "lastname"
            :value "Test"}

           {:objectTypeId "0-1"
            :name "email"
            :value "test@test.com"}

           {:objectTypeId "0-1"
            :name "company"
            :value "Test"}]

          :legalConsentOptions
          {:consent
           {:consentToProcess true
            :text "Sending your data is consent to contact you. For further details on how your personal data will be processed and how your consent will be managed, refer to the Convex Privacy Policy."
            :communications
            [{:subscriptionTypeId 999
              :value true
              :text "I agree to receive other communications from Convex.world."}]}}})}))

  (select-keys @response [:status :body])

  )
