(ns convex.web.rest.client

  "Access to REST endpoint."

  (:require [clojure.data.json  :as json]
            [convex.cell        :as $.cell]
            [convex.clj         :as $.clj]
            [convex.key-pair    :as $.key-pair]
            [org.httpkit.client :as http]))


;;;;;;;;;; Miscellaneous


(defn sig

  "Signs the given hash (as hex string) and returns the signature (as a hex string)."
  
  [key-pair hex-hash]

  (-> ($.key-pair/sign-hash key-pair
                            ($.cell/hash<-hex hex-hash))
      ($.clj/blob->hex)))


;;;;;;;;;; Calling REST endpoints


(defn GET-public-v1-account
  
  [server-url address]

  (http/get (str server-url
                 "/api/v1/accounts/"
                 address)))



(defn POST-public-v1-createAccount

  [server-url account-key]

  (http/post (str server-url
                  "/api/v1/createAccount")
             (json/write-str {:accountKey account-key})))



(defn POST-v1-faucet
  
  [server-url {:keys [_address _amount] :as body}]

  (http/post (str server-url "/api/v1/faucet")
             (json/write-str body)))



(defn POST-public-v1-query

  [server-url body]

  (http/post (str server-url
                  "/api/v1/query")
             (json/write-str body)))



(defn POST-public-v1-transaction-prepare 

  [server-url body]

  (http/post (str server-url
                  "/api/v1/transaction/prepare")
             (json/write-str body)))



(defn POST-public-v1-transaction-prepare' 

  [server-url body]

  (-> @(POST-public-v1-transaction-prepare server-url
                                           body)
      (:body)
      (json/read-str :key-fn keyword)))



(defn POST-public-v1-transaction-submit

  [server-url body]

  (http/post (str server-url
                  "/api/v1/transaction/submit")
             (json/write-str body)))



(defn POST-public-v1-transaction-submit'

  [server-url body]

  (-> @(POST-public-v1-transaction-submit server-url body)
      (:body)
      (json/read-str :key-fn keyword)))
