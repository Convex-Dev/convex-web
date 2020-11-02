(ns convex-web.site-test
  (:require [convex-web.component]
            [convex-web.encoding :as encoding]
            [convex-web.test :refer [make-system-fixture]]

            [clojure.test :refer :all]

            [com.stuartsierra.component]
            [org.httpkit.client :as http]))

(def system nil)

(use-fixtures :once (make-system-fixture #'system))

(defn server-url []
  (str "http://localhost:" (get-in system [:config :config :web-server :port])))

(deftest accounts-test
  (let [latest-accounts-response @(http/get (str (server-url) "/api/internal/accounts"))
        latest-accounts-body (encoding/transit-decode-string (:body latest-accounts-response))]

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

              account (encoding/transit-decode-string body)
              account-no-env (dissoc (get account :convex-web.account/status) :convex-web.account-status/environment)]
          (is (= 200 status)))))

    (testing "Range 10-15"
      (let [{:keys [status body]} @(http/get (str (server-url) "/api/internal/accounts?start=10&end=15"))

            body (encoding/transit-decode-string body)]
        (is (= 200 status))

        (is (= [:start :end :total] (keys (:meta body))))

        (is (= {:start 10 :end 15} (select-keys (:meta body) [:start :end])))))

    (testing "Invalid Range"
      (let [{:keys [status body]} @(http/get (str (server-url) "/api/internal/accounts?start=100&end=150"))]
        (is (= 400 status))

        (is (= {:error {:message "Invalid end: 150."}}
               (encoding/transit-decode-string body)))))))