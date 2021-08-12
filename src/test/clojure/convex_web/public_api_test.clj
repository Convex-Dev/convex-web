(ns convex-web.public-api-test
  (:require 
   [convex-web.component]
   [convex-web.client :as client]
   [convex-web.config :as config]
   [convex-web.web-server :as web-server]
   [convex-web.convex :as convex]
   [convex-web.system :as sys]
   [convex-web.test :as test]
   
   [clojure.test :refer [deftest is testing use-fixtures]]
   [clojure.data.json :as json]
   
   [ring.mock.request :as mock]
   [com.stuartsierra.component]
   [org.httpkit.client :as http])
  
  (:import 
   (convex.core.crypto AKeyPair)
   (convex.core.data Hash AccountKey)))

(def system nil)

(use-fixtures :once (test/make-system-fixture #'system))

(defn convex-world-address []
  (sys/convex-world-address system))

(defn convex-world-address-long []
  (.longValue (convex-world-address)))

(defn convex-world-key-pair ^String []
  (sys/convex-world-key-pair system))

(defn convex-world-account-checksum-hex ^String []
  (sys/convex-world-account-checksum-hex system))

(defn public-api-handler []
  (web-server/public-api-handler system))

(defn server-url []
  (str "http://localhost:" (get-in system [:config :config :web-server :port])))

(defn create-account []
  (let [^AKeyPair generated-key-pair (AKeyPair/generate)
        ^AccountKey account-key (.getAccountKey generated-key-pair)
        ^String account-public-key (.toChecksumHex account-key)
        
        handler (public-api-handler)
        
        response (handler (-> (mock/request :post "/api/v1/createAccount")
                            (mock/json-body {:accountKey account-public-key})))
        
        {generated-address :address} (json/read-str (get response :body) :key-fn keyword)
        
        response (handler (-> (mock/request :post "/api/v1/faucet")
                            (mock/json-body {:address generated-address
                                             :amount 1000000})))
        
        response-body (json/read-str (get response :body) :key-fn keyword)]
    
    {:generated-key-pair generated-key-pair
     :generated-address generated-address
     :account-key account-key
     :account-public-key account-public-key}))

(deftest create-account-test
  (testing "Create new Account"
    (let [^AKeyPair generated-key-pair (AKeyPair/generate)
          ^AccountKey account-key (.getAccountKey generated-key-pair)
          ^String account-public-key (.toChecksumHex account-key)
          
          handler (public-api-handler)]
      
      (testing "Create a new Account with Public Key"
        (let [response (handler (-> (mock/request :post "/api/v1/createAccount")
                                  (mock/json-body {:accountKey account-public-key})))
              
              body-decoded (json/read-str (get response :body) :key-fn keyword)]
          (is (= 200 (:status response)))
          (is (int? (:address body-decoded)))))
      
      
      (testing "Bad request"
        (let [response (handler (-> (mock/request :post "/api/v1/createAccount")
                                  (mock/json-body {})))
              
              body-decoded (json/read-str (get response :body) :key-fn keyword)]
          (is (= 400 (:status response)))
          (is (= {:errorCode "MISSING"
                  :source "Server"
                  :value "Missing account key."}
                body-decoded)))
        
        (let [response (handler (-> (mock/request :post "/api/v1/createAccount")
                                  (mock/json-body nil)))
              
              body-decoded (json/read-str (get response :body) :key-fn keyword)]
          (is (= 400 (:status response)))
          (is (= {:errorCode "MISSING"
                  :source "Server"
                  :value "Missing account key."}
                body-decoded)))
        
        (let [response (handler (-> (mock/request :post "/api/v1/createAccount")
                                  (mock/json-body {:accountKey nil})))
              
              body-decoded (json/read-str (get response :body) :key-fn keyword)]
          (is (= 400 (:status response)))
          (is (= {:errorCode "MISSING"
                  :source "Server"
                  :value "Missing account key."}
                body-decoded)))
        
        (let [response (handler (-> (mock/request :post "/api/v1/createAccount")
                                  (mock/json-body {:accountKey "      "})))
              
              body-decoded (json/read-str (get response :body) :key-fn keyword)]
          (is (= 400 (:status response)))
          (is (= {:errorCode "MISSING"
                  :source "Server"
                  :value "Missing account key."}
                body-decoded))))
      
      (testing "Incorrect Account Key"
        (let [response (handler (-> (mock/request :post "/api/v1/createAccount")
                                  (mock/json-body {:accountKey "123"})))
              
              body-decoded (json/read-str (get response :body) :key-fn keyword)]
          (is (= 400 (:status response)))
          (is (= {:errorCode "INCORRECT"
                  :source "Server"
                  :value "Invalid Account Key."}
                body-decoded)))))))

(deftest create-account-and-topup-test
  (testing "Create new Account and top up"
    (let [^AKeyPair generated-key-pair (AKeyPair/generate)
          ^AccountKey account-key (.getAccountKey generated-key-pair)
          ^String account-public-key (.toChecksumHex account-key)

          handler (public-api-handler)]

      (testing "Create a new Account with Public Key"
        (let [response (handler (-> (mock/request :post "/api/v1/createAccount")
                                    (mock/json-body {:accountKey account-public-key})))

              {generated-address :address} (json/read-str (get response :body) :key-fn keyword)]

          (is (= 200 (:status response)))
          (is (int? generated-address))

          (testing "Top up Account"
            (let [amount 1000

                  response (handler (-> (mock/request :post "/api/v1/faucet")
                                        (mock/json-body {:address generated-address
                                                         :amount amount})))
                  response-body (json/read-str (get response :body) :key-fn keyword)]

              (is (= 200 (get response :status)))
              (is (= {:address generated-address :amount amount :value amount} response-body)))))))))

(deftest address-test
  (let [handler (public-api-handler)]
    (testing "Get Account by Address"
      (let [response (handler (mock/request :get (str "/api/v1/accounts/" (convex-world-address-long))))
            response-body (json/read-str (get response :body) :key-fn keyword)]
        (is (= 200 (get response :status)))
        (is (= #{:account-key
                 :controller
                 :address
                 :allowance
                 :balance
                 :environment
                 :isActor
                 :isLibrary
                 :memorySize
                 :sequence
                 :type
                 :exports}
              (set (keys response-body)))))
      
      (let [response (handler (mock/request :get "/api/v1/accounts/1267650600228229401496703205376"))
            response-body (json/read-str (get response :body) :key-fn keyword)]
        (is (= 400 (get response :status)))
        (is (= {:errorCode "INCORRECT"
                :source "Server"
                :value "Can't coerce \"1267650600228229401496703205376\" to convex.core.data.Address."}
              response-body)))
      
      (let [response (handler (mock/request :get "/api/v1/accounts/-100"))
            response-body (json/read-str (get response :body) :key-fn keyword)]
        (is (= 404 (get response :status)))
        (is (= {:errorCode "NOBODY"
                :value "The Account for this Address does not exist."
                :source "Server"} response-body))))))

(deftest query-test
  (testing "Set"
    (let [response ((public-api-handler) (-> (mock/request :post "/api/v1/query")
                                           (mock/json-body {:address (convex-world-address-long) 
                                                            :source "#{1}"})))
          
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 200 (get response :status)))
      ;; JSON does not have sets, so the encoder uses a vector instead.
      (is (= {:value [1]} response-body))))
  
  (testing "List"
    (let [response ((public-api-handler) (-> (mock/request :post "/api/v1/query")
                                           (mock/json-body {:address (convex-world-address-long) 
                                                            :source "(list 1 2 3)"})))
          
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 200 (get response :status)))
      (is (= {:value [1 2 3]} response-body))))
  
  (testing "Valid"
    (let [response ((public-api-handler) (-> (mock/request :post "/api/v1/query")
                                           (mock/json-body {:address (convex-world-address-long) 
                                                            :source "1"})))
          
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 200 (get response :status)))
      (is (= {:value 1} response-body)))
    
    (let [response ((public-api-handler) (-> (mock/request :post "/api/v1/query")
                                           (mock/json-body {:address (str "#" (convex-world-address-long)) 
                                                            :source "1"})))
          
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 200 (get response :status)))
      (is (= {:value 1} response-body))))
  
  (testing "Syntax error"
    (let [response ((public-api-handler) (-> (mock/request :post "/api/v1/query")
                                           (mock/json-body {:address (convex-world-address-long) 
                                                            :source "(inc 1"})))
          
          response-body (json/read-str (get response :body) :key-fn keyword)]
      
      (is (= 400 (get response :status)))
      (is (= {:errorCode "INCORRECT",
              :source "Server",
              :value "Reader error: -1..-1 <missing ')'>"}
            
            response-body))))
  
  (testing "Non-existent address"
    (let [response ((public-api-handler) (-> (mock/request :post "/api/v1/query")
                                           (mock/json-body {:address 1000 
                                                            :source "(map inc 1)"})))
          
          response-body (json/read-str (get response :body) :key-fn keyword)]
      
      (is (= 200 (get response :status)))
      (is (= {:errorCode "NOBODY"
              :source "CVM"
              :value "ErrorValue[:NOBODY] : Account does not exist for query: #1000"}
            response-body))))
  
  (testing "Type error"
    (let [response ((public-api-handler) (-> (mock/request :post "/api/v1/query")
                                           (mock/json-body {:address (convex-world-address-long) 
                                                            :source "(map inc 1)"})))
          
          response-body (json/read-str (get response :body) :key-fn keyword)]
      
      (is (= 200 (get response :status)))
      (is (= {:errorCode "CAST"
              :source "CVM"
              :value "ErrorValue[:CAST] : Can't convert value of type Long to type Sequence\nIn function: map"}
            response-body)))))

(deftest prepare-test
  (testing "Address doesn't exist"
    (let [prepare-url (str (server-url) "/api/v1/transaction/prepare")
          prepare-body (json/write-str {:address 999 :source "(inc 1)"})
          prepare-response @(http/post prepare-url {:body prepare-body})]
      (is (= 200 (get prepare-response :status)))))

  (testing "Incorrect"
    (testing "No payload"
      (let [prepare-url (str (server-url) "/api/v1/transaction/prepare")
            prepare-response @(http/post prepare-url nil)
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= {:errorCode "MISSING"
                :source "Server"
                :value "Source is required."}
               prepare-response-body))))

    (testing "Invalid Address"
      (let [prepare-url (str (server-url) "/api/v1/transaction/prepare")
            prepare-body (json/write-str {:address ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= {:errorCode "MISSING"
                :source "Server"
                :value "Source is required."}
               prepare-response-body))))

    (testing "Missing Source"
      (let [prepare-url (str (server-url) "/api/v1/transaction/prepare")
            prepare-body (json/write-str {:address (convex-world-address-long) :source ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= {:errorCode "MISSING"
                :source "Server"
                :value "Source is required."}
               prepare-response-body))))))

(deftest submit-test
  (testing "Incorrect"
    (testing "Invalid Address"
      (let [prepare-url (str (server-url) "/api/v1/transaction/submit")
            prepare-body (json/write-str {:address ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]
        
        (is (= 400 (get prepare-response :status)))
        (is (= {:errorCode "INCORRECT"
                :source "Server"
                :value "Invalid address: "}
              prepare-response-body))))
    
    (testing "Invalid Hash"
      (let [prepare-url (str (server-url) "/api/v1/transaction/submit")
            prepare-body (json/write-str {:address (convex-world-address-long) :hash ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]
        
        (is (= 400 (get prepare-response :status)))
        (is (= {:errorCode "INCORRECT"
                :source "Server"
                :value "Invalid hash."}
              prepare-response-body))))
    
    (testing "Invalid Signature"
      (let [prepare-url (str (server-url) "/api/v1/transaction/submit")
            prepare-body (json/write-str {:address (convex-world-address-long) :hash "ABC" :sig ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]
        
        (is (= 400 (get prepare-response :status)))
        (is (= {:errorCode "INCORRECT"
                :source "Server"
                :value "Invalid signature."}
              prepare-response-body))))
    
    (testing "Missing Data"
      (let [response @(client/POST-public-v1-transaction-submit
                        (server-url)
                        {:address (convex-world-address-long)
                         :accountKey (convex-world-account-checksum-hex)
                         :hash "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b"
                         :sig (client/sig (convex-world-key-pair) "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b")})
            response-body (json/read-str (get response :body) :key-fn keyword)]
        
        (is (= 404 (get response :status)))
        (is (= {:errorCode "MISSING"
                :source "Peer"}
              (select-keys response-body [:errorCode :source])))))))

(deftest prepare-submit-transaction-test
  (testing "Prepare & submit transaction"
    (testing "Simple inc"
      (let [{test-key-pair :generated-key-pair
             test-address :generated-address
             test-account-public-key :account-public-key} (create-account)
            
            handler (public-api-handler)
            
            
            
            ;; 1. Prepare
            ;; ==========
            
            prepare-uri "/api/v1/transaction/prepare"
            
            prepare-body {:address test-address :source "(inc 1)"}
            
            prepare-response (handler (-> (mock/request :post prepare-uri)
                                        (mock/json-body prepare-body)))
            
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)
            
            
            ;; 2. Submit
            ;; ==========
            
            submit-uri "/api/v1/transaction/submit"
            
            submit-body {:address test-address
                         :accountKey test-account-public-key
                         :hash (get prepare-response-body :hash)
                         :sig (try
                                (.toHexString (.sign test-key-pair (Hash/fromHex (get prepare-response-body :hash))))
                                (catch Exception _
                                  nil))}
            
            submit-response (handler (-> (mock/request :post submit-uri)
                                       (mock/json-body submit-body)))
            
            submit-response-body (json/read-str (get submit-response :body) :key-fn keyword)]
        
        ;; Prepare is successful
        (is (= 200 (get prepare-response :status)))
        
        ;; Prepare response must contain these keys
        (is (= #{:sequence
                 :address
                 :source
                 :lang
                 :hash}
              (set (keys prepare-response-body))))
        
        ;; Submit is successful
        (is (= 200 (get submit-response :status)))
        
        ;; Submit response must contain these keys
        (is (= {:value 2} submit-response-body))
        
        ;; Submit response result value
        (is (= {:value 2} (select-keys submit-response-body [:value])))))
    
    (testing "Cast error"
      (let [{test-key-pair :generated-key-pair
             test-address :generated-address
             test-account-public-key :account-public-key} (create-account)
            
            handler (public-api-handler)
            
            ;; 1. Prepare
            ;; ==========
            
            prepare-uri "/api/v1/transaction/prepare"
            
            prepare-body {:address test-address :source "(map inc 1)"}
            
            prepare-response (handler (-> (mock/request :post prepare-uri)
                                        (mock/json-body prepare-body)))
            
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)
            
            
            ;; 2. Submit
            ;; ==========
            
            submit-uri "/api/v1/transaction/submit"
            
            submit-body {:address test-address
                         :hash (get prepare-response-body :hash)
                         :accountKey test-account-public-key
                         :sig (try
                                (.toHexString (.sign test-key-pair (Hash/fromHex (get prepare-response-body :hash))))
                                (catch Exception _
                                  nil))}
            
            submit-response (handler (-> (mock/request :post submit-uri)
                                       (mock/json-body submit-body)))
            
            submit-response-body (json/read-str (get submit-response :body) :key-fn keyword)]
        
        ;; Prepare is successful, but transaction should fail.
        (is (= 200 (get prepare-response :status)))
        
        ;; Prepare response must contain these keys.
        (is (= #{:sequence
                 :address
                 :source
                 :lang
                 :hash}
              (set (keys prepare-response-body))))
        
        ;; Submit is successful, but the execution failed.
        (is (= 200 (get submit-response :status)))
        
        ;; Submit response with error code.
        (is (= {:errorCode "CAST"
                :source "CVM"
                :value "Can't convert value of type Long to type Sequence"}
              submit-response-body))))))

(deftest faucet-test
  (let [handler (public-api-handler)
        address (convex-world-address-long)]
    (testing "Success"
      (let [amount 1000

            response (handler (-> (mock/request :post "/api/v1/faucet")
                                  (mock/json-body {:address address
                                                   :amount amount})))

            response-body (json/read-str (get response :body) :key-fn keyword)]

        (is (= 200 (get response :status)))
        (is (= {:address 12 :amount amount :value amount} response-body))))

    (testing "Bad request"
      (testing "No payload"
        (let [response @(client/POST-v1-faucet (server-url) nil)
              response-body (json/read-str (get response :body) :key-fn keyword)]

          (is (= 400 (get response :status)))
          (is (= {:errorCode "INCORRECT"
                  :source "Server"
                  :value "Invalid address: "}
                 response-body))))

      (testing "Invalid address"
        (let [address ""

              amount (inc config/max-faucet-amount)

              response @(client/POST-v1-faucet (server-url) {:address address :amount amount})
              response-body (json/read-str (get response :body) :key-fn keyword)]

          (is (= 400 (get response :status)))
          (is (= {:errorCode "INCORRECT"
                  :source "Server"
                  :value "Invalid address: "}
                 response-body))))

      (testing "Invalid amount"
        (let [address (convex-world-address-long)

              amount -1

              response @(client/POST-v1-faucet (server-url) {:address address :amount amount})
              response-body (json/read-str (get response :body) :key-fn keyword)]

          (is (= 400 (get response :status)))
          (is (= {:errorCode "INCORRECT"
                  :source "Server"
                  :value "Invalid amount: -1"}
                 response-body))))

      (testing "Requested amount is greater than allowed"
        (let [address (convex-world-address-long)

              amount (inc config/max-faucet-amount)

              response @(client/POST-v1-faucet (server-url) {:address address :amount amount})
              response-body (json/read-str (get response :body) :key-fn keyword)]

          (is (= 400 (get response :status)))
          (is (= {:errorCode "INCORRECT"
                  :source "Server"
                  :value "You can't request more than 100,000,000."}
                 response-body)))))))
