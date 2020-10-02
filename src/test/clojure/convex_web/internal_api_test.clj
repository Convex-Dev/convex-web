(ns convex-web.internal-api-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]

            [convex-web.specs]
            [convex-web.test :refer [system-fixture spec-fixture]]
            [convex-web.web-server :as web-server]
            [convex-web.transit :as transit]

            [ring.mock.request :as mock]))

(def system nil)

(use-fixtures :each (system-fixture #'system))

(deftest generate-account-test
  (let [handler (web-server/site system)
        response (handler (mock/request :post "/api/internal/generate-account"))
        body (transit/decode-string (get response :body))]
    (is (= "Success!\n" (s/explain-str :convex-web/account body)))

    ;; Test a closed set, which is helpful to "remind" us
    ;; to fix it whenever we add a new key to the response.
    (is (= #{:convex-web.account/created-at
             :convex-web.account/address}
           (set (keys body))))))
