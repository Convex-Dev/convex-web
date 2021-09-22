(ns convex-web.internal-api-test
  (:require 
   [clojure.test :refer [deftest is testing use-fixtures]]
   
   [convex-web.specs]
   [convex-web.lang :as lang]
   [convex-web.test :refer [make-system-fixture transit-body]]
   [convex-web.web-server :as web-server]
   [convex-web.encoding :as encoding]
   [convex-web.system :as sys]
   
   [ring.mock.request :as mock]))

(def system nil)

(use-fixtures :once (make-system-fixture #'system))

(defn site-handler []
  (web-server/site system))

(defn execute-command [body]
  ((web-server/site system) (-> (mock/request :post "/api/internal/commands")
                                (transit-body body))))

(defn execute-query [source]
  (execute-command #:convex-web.command {:id (java.util.UUID/randomUUID)
                                         
                                         :timestamp (System/currentTimeMillis)
                                         
                                         :mode :convex-web.command.mode/query
                                         
                                         :address
                                         (.longValue (sys/convex-world-address system))
                                         
                                         :query 
                                         {:convex-web.query/source source
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
                          (handler (-> 
                                     (mock/request :post "/api/internal/commands")
                                     (transit-body body))))]
    
    (testing "Invalid"
      (let [response (execute-command {})]
        (is (= 400 (:status response)))))
    
    (testing "Forbidden"
      (let [response (execute-command {:convex-web.command/mode :convex-web.command.mode/transaction})
            response-body (encoding/transit-decode-string (get response :body))]
        
        (is (= 403 (:status response)))
        (is (= {:error {:message "Unauthorized."}} response-body))))
    
    (testing "Query"
      (let [response (execute-command 
                       #:convex-web.command {:id (java.util.UUID/randomUUID)
                                             :timestamp (System/currentTimeMillis)
                                             :mode :convex-web.command.mode/query
                                             :query 
                                             {:convex-web.query/source "(inc 1)"
                                              :convex-web.query/language :convex-lisp}})
            
            {:convex-web.command/keys [result] :as body}
            (encoding/transit-decode-string (get response :body))]
        
        (is (= {:convex-web.result/type "Long", 
                :convex-web.result/value "2"}
              
              (select-keys result [:convex-web.result/type :convex-web.result/value])))
        
        (is (= {:convex-web.command/status :convex-web.command.status/success
                :convex-web.command/mode :convex-web.command.mode/query,
                :convex-web.command/query
                {:convex-web.query/language :convex-lisp, 
                 :convex-web.query/source "(inc 1)"}}
              
              (select-keys body [:convex-web.command/mode
                                 :convex-web.command/query
                                 :convex-web.command/status])))))
    
    (testing "Transaction"
      (let [response (execute-command #:convex-web.command {:timestamp (System/currentTimeMillis)
                                                            :mode :convex-web.command.mode/transaction
                                                            :transaction 
                                                            {:convex-web.transaction/type :convex-web.transaction.type/invoke
                                                             :convex-web.transaction/source "(inc 1)"
                                                             :convex-web.transaction/language :convex-lisp}})]
        
        (is (= 403 (get response :status)))))))

(deftest examples-test
  (testing "Self Balance"
    (let [source (get-in lang/convex-lisp-examples [:self-balance :source])
          
          response (execute-query source)
          
          body (encoding/transit-decode-string (get response :body))]
      
      (is (= {:convex-web.command/mode :convex-web.command.mode/query,
              :convex-web.command/status :convex-web.command.status/success
              :convex-web.command/query
              {:convex-web.query/language :convex-lisp, 
               :convex-web.query/source "*balance*"},}
            
            (select-keys body [:convex-web.command/mode
                               :convex-web.command/status
                               :convex-web.command/query])))
      
      (is (get-in body [:convex-web.command/result :convex-web.result/value]))))
  
  (testing "Self Address"
    (let [source (get-in lang/convex-lisp-examples [:self-address :source])
          
          response (execute-query source)
          
          body (encoding/transit-decode-string (get response :body))]
      
      (is (= {:convex-web.command/mode :convex-web.command.mode/query,
              :convex-web.command/query
              {:convex-web.query/language :convex-lisp, :convex-web.query/source "*address*"},
              :convex-web.command/status :convex-web.command.status/success}
            
            (select-keys body [:convex-web.command/mode
                               :convex-web.command/query
                               :convex-web.command/status])))))
  
  (testing "Check Balance"
    (let [source (get-in lang/convex-lisp-examples [:check-balance :source])
          
          response (execute-query source)
          
          body (encoding/transit-decode-string (get response :body))]
      
      (is (= {:convex-web.command/mode :convex-web.command.mode/query,
              :convex-web.command/query
              {:convex-web.query/language :convex-lisp, :convex-web.query/source "(balance #11)"},
              :convex-web.command/status :convex-web.command.status/success}
            
            (select-keys body [:convex-web.command/mode
                               :convex-web.command/query
                               :convex-web.command/status])))))
  
  (testing "Transfer"
    (let [source (get-in lang/convex-lisp-examples [:transfer :source])
          
          response (execute-query source)
          
          body (encoding/transit-decode-string (get response :body))]
      
      (is (= {:convex-web.command/status :convex-web.command.status/success
              :convex-web.command/mode :convex-web.command.mode/query
              :convex-web.command/query
              {:convex-web.query/language :convex-lisp, 
               :convex-web.query/source "(transfer #11 1000)"}}
            
            (select-keys body [:convex-web.command/metadata
                               :convex-web.command/mode
                               :convex-web.command/query
                               :convex-web.command/status])))))
  
  (testing "Simple Storage Actor"
    (let [source (get-in lang/convex-lisp-examples [:simple-storage-actor :source])
          
          response (execute-query source)
          
          body (encoding/transit-decode-string (get response :body))]
      
      (is (= #:convex-web.command{:mode :convex-web.command.mode/query,
                                  :query
                                  #:convex-web.query{:language :convex-lisp,
                                                     :source
                                                     "(def storage-example-address\n              (deploy '(do (def stored-data nil)\n                           (defn get ^{:callable? true} [] stored-data)\n                           (defn set ^{:callable? true} [x] (def stored-data x)))))"},
                                  :status :convex-web.command.status/success}
            
            (select-keys body [:convex-web.command/metadata
                               :convex-web.command/mode
                               :convex-web.command/query
                               :convex-web.command/status])))))
  
  (testing "Subcurrency Actor"
    (let [source (get-in lang/convex-lisp-examples [:subcurrency-actor :source])
          
          response (execute-query source)
          
          body (encoding/transit-decode-string (get response :body))]
      
      (is (= #:convex-web.command{:mode :convex-web.command.mode/query,
                                  :query
                                  #:convex-web.query{:language :convex-lisp,
                                                     :source
                                                     "(deploy '(do (def owner *caller*)\n                   (defn contract-transfer ^{:callable? true}\n                     [receiver amount]\n                     (assert (= owner *caller*))\n                     (transfer receiver amount))\n                   (defn contract-balance ^{:callable? true} [] *balance*)))"},
                                  :status :convex-web.command.status/success}
            
            (select-keys body [:convex-web.command/metadata
                               :convex-web.command/mode
                               :convex-web.command/query
                               :convex-web.command/status]))))))
