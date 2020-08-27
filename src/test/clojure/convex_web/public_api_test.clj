(ns convex-web.public-api-test
  (:require [convex-web.component]
            [convex-web.client :as client]
            [convex-web.config :as config]

            [clojure.test :refer :all]
            [clojure.data.json :as json]

            [com.stuartsierra.component]
            [org.httpkit.client :as http])
  (:import (convex.core Init)
           (convex.core.crypto Hash)))

(def system nil)

(use-fixtures :each (fn [f]
                      (let [system (com.stuartsierra.component/start
                                     (convex-web.component/system :test))]

                        (alter-var-root #'system (constantly system))

                        (f)

                        (com.stuartsierra.component/stop system))))

(defn server-url []
  (str "http://localhost:" (get-in system [:config :config :web-server :port])))

(deftest prepare-test
  (testing "Valid"
    (testing "Address doesn't exist"
      (let [prepare-url (str (server-url) "/api/v1/transaction/prepare")
            prepare-body (json/write-str {:address "8d4da977c8828050c7e9f00e4800f4ab6137e3da4088d78220ffac81e85cc6e0" :source "(inc 1)"})
            prepare-response @(http/post prepare-url {:body prepare-body})]
        (is (= 200 (get prepare-response :status))))))

  (testing "Incorrect"
    (testing "No payload"
      (let [prepare-url (str (server-url) "/api/v1/transaction/prepare")
            prepare-response @(http/post prepare-url nil)
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= "Invalid address." (get-in prepare-response-body [:error :message])))))

    (testing "Invalid Address"
      (let [prepare-url (str (server-url) "/api/v1/transaction/prepare")
            prepare-body (json/write-str {:address ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= "Invalid address." (get-in prepare-response-body [:error :message])))))

    (testing "Invalid Source"
      (let [prepare-url (str (server-url) "/api/v1/transaction/prepare")
            prepare-body (json/write-str {:address "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f" :source ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= "Invalid source." (get-in prepare-response-body [:error :message])))))))

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
            prepare-body (json/write-str {:address "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f" :hash ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= "Invalid hash." (get-in prepare-response-body [:error :message])))))

    (testing "Invalid Signature"
      (let [prepare-url (str (server-url) "/api/v1/transaction/submit")
            prepare-body (json/write-str {:address "7e66429ca9c10e68efae2dcbf1804f0f6b3369c7164a3187d6233683c258710f" :hash "ABC" :sig ""})
            prepare-response @(http/post prepare-url {:body prepare-body})
            prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)]

        (is (= 400 (get prepare-response :status)))
        (is (= "Invalid signature." (get-in prepare-response-body [:error :message])))))

    (testing "Missing Data"
      (let [response @(client/POST-public-v1-transaction-submit
                        (server-url)
                        {:address (.toChecksumHex Init/HERO)
                         :hash "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b"
                         :sig (client/sig Init/HERO_KP "4cf64e350799858086d05fc003c3fc2b7c8407e8b92574f80fb66a31e8a4e01b")})
            response-body (json/read-str (get response :body) :key-fn keyword)]

        (is (= 400 (get response :status)))
        (is (= "You need to prepare the transaction before submitting." (get-in response-body [:error :message])))))))

(deftest prepare-submit-transaction-test
  (testing "Prepare transaction"
    (let [hero-key-pair Init/HERO_KP
          hero-address (.getAddress hero-key-pair)
          hero-address-str (.toHexString hero-address)

          ;; Prepare
          ;; ==========
          prepare-url (str (server-url) "/api/v1/transaction/prepare")
          prepare-body (json/write-str {:address hero-address-str :source "(inc 1)"})
          prepare-response @(http/post prepare-url {:body prepare-body})
          prepare-response-body (json/read-str (get prepare-response :body) :key-fn keyword)

          ;; Submit
          ;; ==========

          submit-url (str (server-url) "/api/v1/transaction/submit")

          submit-body (json/write-str {:address (.toHexString hero-address)
                                       :hash (get prepare-response-body :hash)
                                       :sig (.toHexString (.sign hero-key-pair (Hash/fromHex (get prepare-response-body :hash))))})

          submit-response @(http/post submit-url {:body submit-body})

          submit-response-body (json/read-str (get submit-response :body) :key-fn keyword)]

      (is (= 200 (get prepare-response :status)))
      (is (= [:sequence-number
              :address
              :source
              :hash]
             (keys prepare-response-body)))

      (is (= 200 (get submit-response :status)))
      (is (= #{:id :value} (set (keys submit-response-body))))
      (is (= {:value 2} (select-keys submit-response-body [:value]))))))

(deftest faucet-test
  (let [address "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71"]
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
        (let [address "2ef2f47F5F6BC609B416512938bAc7e015788019326f50506beFE05527da2d71"

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
