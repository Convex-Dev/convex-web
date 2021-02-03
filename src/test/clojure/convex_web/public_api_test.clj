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
        (let [response (handler (-> (mock/request :post "/api/v1/create-account")
                                    (mock/json-body {:public_key account-public-key})))

              body-decoded (json/read-str (get response :body) :key-fn keyword)]
          (is (= 200 (:status response)))
          (is (int? (:address body-decoded)))))


      (testing "Bad request"
        (let [response (handler (-> (mock/request :post "/api/v1/create-account")
                                    (mock/json-body {})))

              body-decoded (json/read-str (get response :body) :key-fn keyword)]
          (is (= 400 (:status response)))
          (is (= {:error {:message "Missing public key."}} body-decoded)))

        (let [response (handler (-> (mock/request :post "/api/v1/create-account")
                                    (mock/json-body nil)))

              body-decoded (json/read-str (get response :body) :key-fn keyword)]
          (is (= 400 (:status response)))
          (is (= {:error {:message "Missing public key."}} body-decoded)))

        (let [response (handler (-> (mock/request :post "/api/v1/create-account")
                                    (mock/json-body {:public_key nil})))

              body-decoded (json/read-str (get response :body) :key-fn keyword)]
          (is (= 400 (:status response)))
          (is (= {:error {:message "Missing public key."}} body-decoded)))

        (let [response (handler (-> (mock/request :post "/api/v1/create-account")
                                    (mock/json-body {:public_key "      "})))

              body-decoded (json/read-str (get response :body) :key-fn keyword)]
          (is (= 400 (:status response)))
          (is (= {:error {:message "Missing public key."}} body-decoded))))

      (let [response (handler (-> (mock/request :post "/api/v1/create-account")
                                  (mock/json-body {:public_key "abc"})))

            body-decoded (json/read-str (get response :body) :key-fn keyword)]
        (is (= 400 (:status response)))
        (is (= {:error {:message ":UNDECLARED \"xabc\""}} body-decoded))))))

(deftest address-test
  (testing "Get Account by Address"
    (let [response @(client/GET-public-v1-account (server-url) (.longValue Init/HERO))
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 200 (get response :status)))
      (is (= #{:address
               :allowance
               :balance
               :environment
               :is_actor
               :is_library
               :memory_size
               :sequence
               :type}
             (set (keys response-body)))))

    (let [response @(client/GET-public-v1-account (server-url) 1267650600228229401496703205376)
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 400 (get response :status)))
      (is (= {:error {:message "Invalid Address."}} response-body)))

    (let [response @(client/GET-public-v1-account (server-url) -100)
          response-body (json/read-str (get response :body) :key-fn keyword)]
      (is (= 404 (get response :status)))
      (is (= {:error {:message "The Account for this Address does not exist."}} response-body)))))

(deftest query-test
  (testing "Valid"
    (let [response @(client/POST-public-v1-query (server-url) {:address (.longValue Init/HERO) :source "1"})
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
        (is (= {:error {:message "Syntax error."}} response-body)))))

  (testing "Syntax error"
    (let [response @(client/POST-public-v1-query (server-url) {:address (.longValue Init/HERO) :source "(inc 1"})
          response-body (json/read-str (get response :body) :key-fn keyword)]

      (is (= 400 (get response :status)))
      (is (= {:error {:message "Syntax error."}} response-body))))

  (testing "Non-existent address"
    (let [response @(client/POST-public-v1-query (server-url) {:address 1000
                                                               :source "(map inc 1)"})
          response-body (json/read-str (get response :body) :key-fn keyword)]

      (is (= 200 (get response :status)))
      ;; FIXME
      (is (= {:error-code "NOBODY"} (select-keys response-body [:error-code])))))

  (testing "Type error"
    (let [response @(client/POST-public-v1-query (server-url) {:address (.longValue Init/HERO) :source "(map inc 1)"})
          response-body (json/read-str (get response :body) :key-fn keyword)]

      (is (= 200 (get response :status)))
      (is (= {:error-code "CAST"
              :value "ErrorValue[:CAST] : Can't convert 1 of class convex.core.data.prim.CVMLong to class class convex.core.data.ASequence
In function: map"} response-body)))))

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
        (is (= "Invalid address: " (get-in prepare-response-body [:error :message])))))

    (testing "Invalid Address"
      (let [prepare-url (str (server-url) "/api/v1/transaction/prepare")
            prepare-body (json/write-str {:address ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= "Invalid address: " (get-in prepare-response-body [:error :message])))))

    (testing "Missing Source"
      (let [prepare-url (str (server-url) "/api/v1/transaction/prepare")
            prepare-body (json/write-str {:address (.longValue Init/HERO) :source ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= "Source is required." (get-in prepare-response-body [:error :message])))))))

(deftest submit-test
  (testing "Incorrect"
    (testing "Invalid Address"
      (let [prepare-url (str (server-url) "/api/v1/transaction/submit")
            prepare-body (json/write-str {:address ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= "Invalid address." (get-in prepare-response-body [:error :message])))))

    (testing "Invalid Hash"
      (let [prepare-url (str (server-url) "/api/v1/transaction/submit")
            prepare-body (json/write-str {:address (.longValue Init/HERO) :hash ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= "Invalid hash." (get-in prepare-response-body [:error :message])))))

    (testing "Invalid Signature"
      (let [prepare-url (str (server-url) "/api/v1/transaction/submit")
            prepare-body (json/write-str {:address (.longValue Init/HERO) :hash "ABC" :sig ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= "Invalid signature." (get-in prepare-response-body [:error :message])))))

    (testing "Missing Data"
      (let [response @(client/POST-public-v1-transaction-submit
                        (server-url)
                        {:address (.longValue Init/HERO)
                         :account_key (.toChecksumHex (.getAccountKey Init/HERO_KP))
                         :hash "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b"
                         :sig (client/sig Init/HERO_KP "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b")})
            response-body (json/read-str (get response :body) :key-fn keyword)]

        (is (= 400 (get response :status)))
        (is (= "You need to prepare the transaction before submitting." (get-in response-body [:error :message])))))))

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
                         :account_key test-account-key
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
        (is (= #{:sequence_number
                 :address
                 :source
                 :lang
                 :hash}
               (set (keys prepare-response-body))))

        ;; Submit is successful
        (is (= 200 (get submit-response :status)))

        ;; Submit response must contain these keys
        (is (= #{:id :value} (set (keys submit-response-body))))

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
                         :account_key test-account-key
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
        (is (= #{:sequence_number
                 :address
                 :source
                 :lang
                 :hash}
               (set (keys prepare-response-body))))

        ;; Submit is successful, but the execution failed.
        (is (= 200 (get submit-response :status)))

        ;; Submit response must contain these keys.
        (is (= #{:id :value :error-code} (set (keys submit-response-body))))

        ;; Submit response with error code.
        (is (= {:error-code "CAST"
                :value "Can't convert 1 of class convex.core.data.prim.CVMLong to class class convex.core.data.ASequence"}
               (select-keys submit-response-body [:error-code :value])))))))

(deftest faucet-test
  (let [address (.longValue Init/HERO)]
    (testing "Success"
      (let [amount 1000

            response @(client/POST-v1-faucet (server-url) {:address address :amount amount})
            response-body (json/read-str (get response :body) :key-fn keyword)]

        (is (= 200 (get response :status)))
        (is (= #{:id :address :amount :value} (set (keys response-body))))))

    (testing "Bad request"
      (testing "No payload"
        (let [response @(client/POST-v1-faucet (server-url) nil)
              response-body (json/read-str (get response :body) :key-fn keyword)]

          (is (= 400 (get response :status)))
          (is (= "Invalid address." (get-in response-body [:error :message])))))

      (testing "Invalid address"
        (let [address ""

              amount (inc config/max-faucet-amount)

              response @(client/POST-v1-faucet (server-url) {:address address :amount amount})
              response-body (json/read-str (get response :body) :key-fn keyword)]

          (is (= 400 (get response :status)))
          (is (= "Invalid address." (get-in response-body [:error :message])))))

      (testing "Invalid amount"
        (let [address (.longValue Init/HERO)

              amount -1

              response @(client/POST-v1-faucet (server-url) {:address address :amount amount})
              response-body (json/read-str (get response :body) :key-fn keyword)]

          (is (= 400 (get response :status)))
          (is (= "Invalid amount." (get-in response-body [:error :message])))))

      (testing "Requested amount is greater than allowed"
        (let [address "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71"

              amount (inc config/max-faucet-amount)

              response @(client/POST-v1-faucet (server-url) {:address address :amount amount})]

          (is (= 400 (get response :status))))))))
