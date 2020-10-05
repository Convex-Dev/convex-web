(ns convex-web.internal-api-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]

            [convex-web.specs]
            [convex-web.test :refer [system-fixture spec-fixture]]
            [convex-web.web-server :as web-server]
            [convex-web.encoding :as encoding]

            [ring.mock.request :as mock]))

(def system nil)

(use-fixtures :each (system-fixture #'system))


(deftest session-test
  (let [handler (web-server/site system)
        response (handler (mock/request :get "/api/internal/session"))
        body (encoding/transit-decode-string (get response :body))]
    (is (= 200 (get response :status)))
    (is (= #{:convex-web.session/id} (set (keys body))))))

(deftest reference-test
  (let [handler (web-server/site system)
        response (handler (mock/request :get "/api/internal/reference"))]
    (is (= 200 (get response :status)))))

(deftest generate-account-test
  (let [handler (web-server/site system)
        response (handler (mock/request :post "/api/internal/generate-account"))
        body (encoding/transit-decode-string (get response :body))]
    (is (= "Success!\n" (s/explain-str :convex-web/account body)))

    ;; Test a closed set, which is helpful to "remind" us
    ;; to fix it whenever we add a new key to the response.
    (is (= #{:convex-web.account/created-at
             :convex-web.account/address}
           (set (keys body))))))

(deftest blocks-test
  (let [handler (web-server/site system)
        response (handler (mock/request :get "/api/internal/blocks-range"))
        body (encoding/transit-decode-string (get response :body))]
    (is (= 200 (get response :status)))
    (is (= #{:convex-web/blocks
             :meta}
           (set (keys body))))))

(deftest accounts-test
  (let [handler (web-server/site system)]

    (testing "Default"
      (let [response (handler (mock/request :get "/api/internal/accounts"))
            body (encoding/transit-decode-string (get response :body))]
        (is (= 200 (get response :status)))
        (is (= #{:convex-web/accounts
                 :meta}
               (set (keys body))))))

    (testing "Not found"
      (let [response (handler (mock/request :get "/api/internal/accounts/x"))
            body (encoding/transit-decode-string (get response :body))]
        (is (= 404 (get response :status)))
        (is (= #{:error} (set (keys body))))
        (is (= "The Account for this Address does not exist." (get-in body [:error :message])))))))
