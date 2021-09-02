(ns convex-web.site-test
  (:require 
   [convex-web.component]
   [convex-web.encoding :as encoding]
   [convex-web.web-server :as web-server]
   [convex-web.test :refer [make-system-fixture]]
   
   [clojure.test :refer :all]
   
   [com.stuartsierra.component]
   [ring.mock.request :as mock]))

(def system nil)

(use-fixtures :once (make-system-fixture #'system))

(deftest accounts-test
  (let [handler (web-server/site-handler system)]
    (testing "Get Accounts"
      (let [request (mock/request :get "/api/internal/accounts")
            response (handler request)
            response-body (encoding/transit-decode-string (:body response))]
        (is (= 200 (:status response)))
        (is (= [:start :end :total] (keys (:meta response-body))))))
    
    (testing "Account Not Found"
      (let [request (mock/request :get "/api/internal/accounts/x")
            response (handler request)]
        (is (= 404 (:status response)))))
    
    (testing "Range 10-15"
      (let [request (mock/request :get "/api/internal/accounts?start=10&end=15")
            response (handler request)
            response-body (encoding/transit-decode-string (:body response))]
        (is (= 200 (:status response)))
        (is (= [:start :end :total] (keys (:meta response-body))))
        (is (= {:start 10 :end 15} (select-keys (:meta response-body) [:start :end])))))
    
    (testing "Invalid end"
      (let [request (mock/request :get "/api/internal/accounts?start=100&end=150")
            response (handler request)
            response-body (encoding/transit-decode-string (:body response))]
        (is (= 400 (:status response)))
        (is (= {:error {:message "Invalid end: 150."}} response-body))))))