(ns convex-web.http-api-test
  (:require [convex-web.component]
            [convex-web.transit :as transit]

            [clojure.test :refer :all]

            [com.stuartsierra.component]
            [org.httpkit.client :as http])
  (:import (org.slf4j.bridge SLF4JBridgeHandler)))

(def system nil)

(use-fixtures :once (fn [f]
                      (SLF4JBridgeHandler/removeHandlersForRootLogger)
                      (SLF4JBridgeHandler/install)

                      (let [system (com.stuartsierra.component/start
                                     (convex-web.component/system :test))]

                        (alter-var-root #'system (constantly system))

                        (f)

                        (com.stuartsierra.component/stop system))))

(defn server-url []
  (str "http://localhost:" (get-in system [:config :config :web-server :port])))

(deftest session-test
  (testing "Get Session"
    (let [{:keys [status body]} @(http/get (str (server-url) "/api/internal/session"))]
      (is (= 200 status))
      (is (= #:convex-web.session{:id nil} (transit/decode-string body))))))

(deftest reference-test
  (testing "Get Reference"
    (let [{:keys [status]} @(http/get (str (server-url) "/api/internal/reference"))]
      (is (= 200 status)))))

(deftest generate-account-test
  (testing "Generate Account"
    (let [{:keys [status body]} @(http/post (str (server-url) "/api/internal/generate-account"))]
      (is (= 403 status))
      (is (= "<h1>Invalid anti-forgery token</h1>" body)))))

(deftest blocks-test
  (testing "Get Blocks"
    (let [{:keys [status body]} @(http/get (str (server-url) "/api/internal/blocks-range"))]
      (is (= 200 status))

      (is (= {:convex-web/blocks []
              :meta {:end 0
                     :start 0
                     :total 0}}
             (transit/decode-string body))))

    (let [{:keys [status body]} @(http/get (str (server-url) "/api/internal/blocks-range?start=10&end=15"))]
      (is (= 400 status))

      (is (= {:error {:message "Invalid end: 15."}}
             (transit/decode-string body))))))

(deftest accounts-test
  (let [latest-accounts-response @(http/get (str (server-url) "/api/internal/accounts"))
        latest-accounts-body (transit/decode-string (:body latest-accounts-response))]

    (testing "Get Latest Accounts"
      (is (= 200 (:status latest-accounts-response)))

      (is (= [:start :end :total] (keys (:meta latest-accounts-body)))))

    (testing "Get Account"
      (testing "Not Found"
        (let [{:keys [status]} @(http/get (str (server-url) "/api/internal/accounts/x"))]
          (is (= 404 status))))

      (testing "Address 4444444444444444444444444444444444444444444444444444444444444444"
        (let [[{:convex-web.account/keys [address]}] (get latest-accounts-body :convex-web/accounts)

              {:keys [status body]} @(http/get (str (server-url) "/api/internal/accounts/" address))

              account (transit/decode-string body)
              account-no-env (dissoc (get account :convex-web.account/status) :convex-web.account-status/environment)]
          (is (= 200 status))

          (is (= #:convex-web.account{:address "4444444444444444444444444444444444444444444444444444444444444444"}
                 (select-keys account [:convex-web.account/address])))

          (is (= #:convex-web.account-status{:actor? false
                                             :library? false
                                             :type :user
                                             :balance 900000000000
                                             :sequence 0}
                 account-no-env)))))

    (testing "Range 10-15"
      (let [{:keys [status body]} @(http/get (str (server-url) "/api/internal/accounts?start=10&end=15"))

            body (transit/decode-string body)]
        (is (= 200 status))

        (is (= [:start :end :total] (keys (:meta body))))

        (is (= {:start 10 :end 15} (select-keys (:meta body) [:start :end])))))

    (testing "Invalid Range"
      (let [{:keys [status body]} @(http/get (str (server-url) "/api/internal/accounts?start=100&end=150"))]
        (is (= 400 status))

        (is (= {:error {:message "Invalid end: 150."}}
               (transit/decode-string body)))))))