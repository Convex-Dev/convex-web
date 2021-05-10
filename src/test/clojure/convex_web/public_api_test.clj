(ns convex-web.public-api-test
  (:require [convex-web.component]
            [convex-web.client :as client]
            [convex-web.config :as config]
            [convex-web.web-server :as web-server]
            [convex-web.test :refer [make-system-fixture]]

            [clojure.test :refer :all]
            [clojure.data.json :as json]

            [ring.mock.request :as mock]
            [com.stuartsierra.component]
            [org.httpkit.client :as http])
  (:import (convex.core Init)
           (convex.core.crypto Hash AKeyPair)
           (convex.core.data AccountKey)))

(def system nil)

(use-fixtures :once (make-system-fixture #'system))

(defn public-api-handler []
  (web-server/public-api-handler system))

(defn server-url []
  (str "http://localhost:" (get-in system [:config :config :web-server :port])))

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

      (let [response (handler (-> (mock/request :post "/api/v1/createAccount")
                                  (mock/json-body {:accountKey "abc"})))

            body-decoded (json/read-str (get response :body) :key-fn keyword)]
        (is (= 400 (:status response)))
        (is (= {:errorCode "UNDECLARED"
                :source "CVM"
                :value ":UNDECLARED \"xabc\""}
               body-decoded))))))

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
  (testing "Get Account by Address"
    (let [response @(client/GET-public-v1-account (server-url) (.longValue Init/HERO))
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 200 (get response :status)))
      (is (= #{:address
               :allowance
               :balance
               :environment
               :isActor
               :isLibrary
               :memorySize
               :sequence
               :type}
             (set (keys response-body)))))

    (let [response @(client/GET-public-v1-account (server-url) 1267650600228229401496703205376)
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 400 (get response :status)))
      (is (= {:errorCode "INCORRECT"
              :source "Server"
              :value "Can't coerce \"1267650600228229401496703205376\" to convex.core.data.Address."}
             response-body)))

    (let [response @(client/GET-public-v1-account (server-url) -100)
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 404 (get response :status)))
      (is (= {:errorCode "NOBODY"
              :value "The Account for this Address does not exist."
              :source "Server"} response-body)))))

(deftest query-test
  (testing "Valid"
    (let [response @(client/POST-public-v1-query (server-url) {:address (.longValue Init/HERO) :source "1"})
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 200 (get response :status)))
      (is (= {:value 1} response-body)))

    (let [response @(client/POST-public-v1-query (server-url) {:address (str "#" (.longValue Init/HERO)) :source "1"})
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 200 (get response :status)))
      (is (= {:value 1} response-body))))

  (testing "Scrypt"
    (let [response @(client/POST-public-v1-query (server-url) {:address (.longValue Init/HERO)
                                                               :source "inc(1)"
                                                               :lang :convex-scrypt})
          response-body (json/read-str (get response :body) :key-fn keyword)]

      (is (= 200 (get response :status)))
      (is (= {:value 2} response-body)))

    (let [response @(client/POST-public-v1-query (server-url) {:address (.longValue Init/HERO)
                                                               :source "reduce(+, 0, [1, 2, 3])"
                                                               :lang :convex-scrypt})
          response-body (json/read-str (get response :body) :key-fn keyword)]

      (is (= 200 (get response :status)))
      (is (= {:value 6} response-body)))

    (let [response1 @(client/POST-public-v1-query (server-url) {:address (.longValue Init/HERO)
                                                                :source (str "balance(address(" (.longValue Init/HERO) "))")
                                                                :lang :convex-scrypt})
          response-body1 (json/read-str (get response1 :body) :key-fn keyword)

          response2 @(client/POST-public-v1-query (server-url) {:address (.longValue Init/HERO)
                                                                :source (str "balance(address(" (.longValue Init/HERO) "))")
                                                                :lang :convex-scrypt})
          response-body2 (json/read-str (get response2 :body) :key-fn keyword)]

      (is (= 200 (get response1 :status)))
      (is (= 200 (get response2 :status)))

      (is (= response-body1 response-body2)))

    (testing "Syntax error"
      (let [response @(client/POST-public-v1-query (server-url) {:address (.longValue Init/HERO)
                                                                 :source "map(inc [1, 2, 3, 4, 5])"
                                                                 :lang :convex-scrypt})
            response-body (json/read-str (get response :body) :key-fn keyword)]

        (is (= 400 (get response :status)))
        (is (= {:errorCode "INCORRECT"
                :source "Server"
                :value "Error while parsing action 'CompilationUnit/CompilationUnit_Action2' at input position (line 1, pos 1):\nmap(inc [1, 2, 3, 4, 5])\n^\n\nconvex.core.exceptions.ParseException: Invalid program."}
               response-body)))))

  (testing "Syntax error"
    (let [response @(client/POST-public-v1-query (server-url) {:address (.longValue Init/HERO) :source "(inc 1"})
          response-body (json/read-str (get response :body) :key-fn keyword)]

      (is (= 400 (get response :status)))
      (is (= {:errorCode "INCORRECT"
              :source "Server"
              :value "Error while parsing action 'Input/ExpressionList/ZeroOrMore/Sequence/Expression/FirstOf/DelimitedExpression/DataStructure/List/FirstOf/Sequence/List_Action1' at input position (line 1, pos 8):\n(inc 1\n       ^\n\nconvex.core.exceptions.ParseException: Expected closing ')'"}
             response-body))))

  (testing "Non-existent address"
    (let [response @(client/POST-public-v1-query (server-url) {:address 1000
                                                               :source "(map inc 1)"})
          response-body (json/read-str (get response :body) :key-fn keyword)]

      (is (= 200 (get response :status)))
      (is (= {:errorCode "NOBODY"
              :source "CVM"
              :value "ErrorValue[:NOBODY] : Account does not exist for query: #1000"}
             response-body))))

  (testing "Type error"
    (let [response @(client/POST-public-v1-query (server-url) {:address (.longValue Init/HERO) :source "(map inc 1)"})
          response-body (json/read-str (get response :body) :key-fn keyword)]

      (is (= 200 (get response :status)))
      (is (= {:errorCode "CAST"
              :source "CVM"
              :value "ErrorValue[:CAST] : Can't convert 1 of class convex.core.data.prim.CVMLong to class class convex.core.data.ASequence
In function: map"}
             response-body)))))

(deftest prepare-test
  (testing "Convex Scrypt"
    (let [response @(client/POST-public-v1-transaction-prepare (server-url) {:address (.longValue Init/HERO)
                                                                             :source "inc(1)"
                                                                             :lang :convex-scrypt})]
      (is (= 200 (get response :status))))

    (testing "Syntax error"
      (let [prepare-url (str (server-url) "/api/v1/transaction/prepare")
            prepare-body (json/write-str {:address (.longValue Init/HERO)
                                          :source "map(inc [1, 2, 3, 4, 5])"
                                          :lang :convex-scrypt})
            response @(http/post prepare-url {:body prepare-body})]
        (is (= 400 (get response :status))))))

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
            prepare-body (json/write-str {:address (.longValue Init/HERO) :source ""})
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
            prepare-body (json/write-str {:address (.longValue Init/HERO) :hash ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= {:errorCode "INCORRECT"
                :source "Server"
                :value "Invalid hash."}
               prepare-response-body))))

    (testing "Invalid Signature"
      (let [prepare-url (str (server-url) "/api/v1/transaction/submit")
            prepare-body (json/write-str {:address (.longValue Init/HERO) :hash "ABC" :sig ""})
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
                        {:address (.longValue Init/HERO)
                         :accountKey (.toChecksumHex (.getAccountKey Init/HERO_KP))
                         :hash "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b"
                         :sig (client/sig Init/HERO_KP "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b")})
            response-body (json/read-str (get response :body) :key-fn keyword)]

        (is (= 400 (get response :status)))
        (is (= {:errorCode "INCORRECT"
                :source "CVM"
                :value "Failed to get Transaction result."}
               response-body))))))

(deftest prepare-submit-transaction-test
  (testing "Prepare & submit transaction"
    (testing "Simple inc"
      (let [test-key-pair Init/VILLAIN_KP
            test-address (.longValue Init/VILLAIN)
            test-account-key (.toChecksumHex (.getAccountKey Init/VILLAIN_KP))

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
                         :accountKey test-account-key
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
      (let [test-key-pair Init/VILLAIN_KP
            test-address (.longValue Init/VILLAIN)
            test-account-key (.toChecksumHex (.getAccountKey Init/VILLAIN_KP))

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
                         :accountKey test-account-key
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
                :value "Can't convert 1 of class convex.core.data.prim.CVMLong to class class convex.core.data.ASequence"}
               submit-response-body))))))

(deftest faucet-test
  (let [handler (public-api-handler)
        address (.longValue Init/HERO)]
    (testing "Success"
      (let [amount 1000

            response (handler (-> (mock/request :post "/api/v1/faucet")
                                  (mock/json-body {:address address
                                                   :amount amount})))

            response-body (json/read-str (get response :body) :key-fn keyword)]

        (is (= 200 (get response :status)))
        (is (= {:address 9 :amount amount :value amount} response-body))))

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
        (let [address (.longValue Init/HERO)

              amount -1

              response @(client/POST-v1-faucet (server-url) {:address address :amount amount})
              response-body (json/read-str (get response :body) :key-fn keyword)]

          (is (= 400 (get response :status)))
          (is (= {:errorCode "INCORRECT"
                  :source "Server"
                  :value "Invalid amount: -1"}
                 response-body))))

      (testing "Requested amount is greater than allowed"
        (let [address (.longValue Init/HERO)

              amount (inc config/max-faucet-amount)

              response @(client/POST-v1-faucet (server-url) {:address address :amount amount})
              response-body (json/read-str (get response :body) :key-fn keyword)]

          (is (= 400 (get response :status)))
          (is (= {:errorCode "INCORRECT"
                  :source "Server"
                  :value "You can't request more than 100,000,000."}
                 response-body)))))))
