(ns convex-web.internal-api-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]

            [convex-web.specs]
            [convex-web.lang :as lang]
            [convex-web.test :refer :all]
            [convex-web.web-server :as web-server]
            [convex-web.encoding :as encoding]

            [ring.mock.request :as mock])
  (:import (convex.core Init)))

(def system nil)

(use-fixtures :once (make-system-fixture #'system))

(defn site-handler []
  (web-server/site system))

(defn execute-command [body]
  ((web-server/site system) (-> (mock/request :post "/api/internal/commands")
                                (transit-body body))))

(defn execute-query [source]
  (execute-command #:convex-web.command {:mode :convex-web.command.mode/query
                                         :address (.longValue Init/HERO)
                                         :query {:convex-web.query/source source
                                                 :convex-web.query/language :convex-lisp}}))

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
    (is (= #{:convex-web/blocks :meta} (set (keys body))))))

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

(deftest command-test
  (let [handler (site-handler)

        execute-command (fn [body]
                          (handler (-> (mock/request :post "/api/internal/commands")
                                       (transit-body body))))]
    (testing "Query"
      (let [response (execute-command #:convex-web.command {:mode :convex-web.command.mode/query
                                                            :address (.longValue Init/HERO)
                                                            :query {:convex-web.query/source "(inc 1)"
                                                                    :convex-web.query/language :convex-lisp}})

            body (encoding/transit-decode-string (get response :body))]

        (is (= #:convex-web.command{:metadata {:type :long}
                                    :mode :convex-web.command.mode/query
                                    :object 2
                                    :query #:convex-web.query{:language :convex-lisp :source "(inc 1)"}
                                    :status :convex-web.command.status/success}

               (select-keys body [:convex-web.command/metadata
                                  :convex-web.command/mode
                                  :convex-web.command/object
                                  :convex-web.command/query
                                  :convex-web.command/status])))))

    (testing "Transaction"
      (let [response (execute-command #:convex-web.command {:mode :convex-web.command.mode/transaction
                                                            :address (.longValue Init/HERO)
                                                            :transaction {:convex-web.transaction/type :convex-web.transaction.type/invoke
                                                                          :convex-web.transaction/source "(inc 1)"
                                                                          :convex-web.transaction/language :convex-lisp}})]

        (is (= 403 (get response :status)))))))

(deftest examples-test
  (testing "Self Balance"
    (let [source (get-in lang/convex-lisp-examples [:self-balance :source])

          response (execute-query source)

          body (encoding/transit-decode-string (get response :body))]

      (is (= #:convex-web.command{:metadata {:type :long}
                                  :mode :convex-web.command.mode/query
                                  :query #:convex-web.query{:language :convex-lisp :source source}
                                  :status :convex-web.command.status/success}

             (select-keys body [:convex-web.command/metadata
                                :convex-web.command/mode
                                :convex-web.command/query
                                :convex-web.command/status])))

      (is (nat-int? (get body :convex-web.command/object)))))

  (testing "Self Address"
    (let [source (get-in lang/convex-lisp-examples [:self-address :source])

          response (execute-query source)

          body (encoding/transit-decode-string (get response :body))]

      (is (= #:convex-web.command{:metadata {:type :address}
                                  :mode :convex-web.command.mode/query
                                  :query #:convex-web.query{:language :convex-lisp :source source}
                                  :status :convex-web.command.status/success}

             (select-keys body [:convex-web.command/metadata
                                :convex-web.command/mode
                                :convex-web.command/query
                                :convex-web.command/status])))

      (is (= #{:address} (set (keys (get body :convex-web.command/object)))))))

  (testing "Check Balance"
    (let [source (get-in lang/convex-lisp-examples [:check-balance :source])

          response (execute-query source)

          body (encoding/transit-decode-string (get response :body))]

      (is (= #:convex-web.command{:metadata {:type :long}
                                  :mode :convex-web.command.mode/query
                                  :query #:convex-web.query{:language :convex-lisp :source source}
                                  :status :convex-web.command.status/success}

             (select-keys body [:convex-web.command/metadata
                                :convex-web.command/mode
                                :convex-web.command/query
                                :convex-web.command/status])))

      (is (nat-int? (get body :convex-web.command/object)))))

  (testing "Transfer"
    (let [source (get-in lang/convex-lisp-examples [:transfer :source])

          response (execute-query source)

          body (encoding/transit-decode-string (get response :body))]

      (is (= #:convex-web.command{:metadata {:type :long}
                                  :mode :convex-web.command.mode/query
                                  :query #:convex-web.query{:language :convex-lisp :source source}
                                  :status :convex-web.command.status/success}

             (select-keys body [:convex-web.command/metadata
                                :convex-web.command/mode
                                :convex-web.command/query
                                :convex-web.command/status])))

      (is (nat-int? (get body :convex-web.command/object)))))

  (testing "Simple Storage Actor"
    (let [source (get-in lang/convex-lisp-examples [:simple-storage-actor :source])

          response (execute-query source)

          body (encoding/transit-decode-string (get response :body))]

      (is (= #:convex-web.command{:metadata {:type :address}
                                  :mode :convex-web.command.mode/query
                                  :query #:convex-web.query{:language :convex-lisp :source source}
                                  :status :convex-web.command.status/success}

             (select-keys body [:convex-web.command/metadata
                                :convex-web.command/mode
                                :convex-web.command/query
                                :convex-web.command/status])))

      (is (= #{:address} (set (keys (get body :convex-web.command/object)))))))

  (testing "Subcurrency Actor"
    (let [source (get-in lang/convex-lisp-examples [:subcurrency-actor :source])

          response (execute-query source)

          body (encoding/transit-decode-string (get response :body))]

      (is (= #:convex-web.command{:metadata {:type :address}
                                  :mode :convex-web.command.mode/query
                                  :query #:convex-web.query{:language :convex-lisp :source source}
                                  :status :convex-web.command.status/success}

             (select-keys body [:convex-web.command/metadata
                                :convex-web.command/mode
                                :convex-web.command/query
                                :convex-web.command/status])))

      (is (= #{:address} (set (keys (get body :convex-web.command/object))))))))

(comment
  (require 'convex-web.internal-api-test)
  (in-ns 'convex-web.internal-api-test)
  
  (run-tests))