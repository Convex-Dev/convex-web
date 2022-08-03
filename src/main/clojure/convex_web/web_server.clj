(ns convex-web.web-server
  (:require
   [convex-web.specs]
   [convex-web.convex :as convex]
   [convex-web.system :as system]
   [convex-web.account :as account]
   [convex-web.session :as session]
   [convex-web.command :as command]
   [convex-web.config :as config]
   [convex-web.encoding :as encoding]
   [convex-web.wallet :as wallet]

   [clojure.set :refer [rename-keys]]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.tools.logging :as log]
   [clojure.pprint :as pprint]
   [clojure.stacktrace :as stacktrace]
   [clojure.data.json :as json]
   [clojure.string :as str]

   [cognitect.anomalies :as anomalies]

   [com.brunobonacci.mulog :as u]
   [expound.alpha :as expound]
   [datalevin.core :as d]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
   [org.httpkit.server :as http-kit]
   [compojure.core :refer [routes GET POST]]
   [compojure.route :as route]
   [hiccup.page :as page]
   [ring.util.anti-forgery]
   [ring.middleware.cors :refer [wrap-cors]])
  (:import
   (java.io InputStream)

   (convex.api Convex)
   (convex.core.crypto ASignature AKeyPair Ed25519KeyPair)
   (convex.core.data Ref SignedData AccountKey ACell Hash Address Blob)
   (convex.core.lang Context)
   (convex.core.lang.impl AExceptional)
   (convex.core Peer State Result Order)
   (convex.core.exceptions MissingDataException)

   (java.time Instant ZonedDateTime)
   (java.time.format DateTimeFormatter)

   (java.util Date)
   (clojure.lang ExceptionInfo)))

(defn ring-session [request]
  (get-in request [:cookies "ring-session" :value]))

(defn session-addresses [context request]
  (let [session (session/find-session (system/db context) (ring-session request))]
    (->> (get session ::session/accounts)
         (map ::account/address)
         (into #{}))))

(defn read-markdown-page [id]
  (let [markdown-pages (edn/read-string (slurp (io/resource "markdown-pages.edn")))]
    (when-let [{:keys [contents] :as markdown-page} (get markdown-pages id)]
      (assoc markdown-page :contents (map
                                       (fn [{:keys [name path]}]
                                         {:name name
                                          :content (slurp (io/resource path))})
                                       contents)))))

(defn stylesheet [href]
  [:link
   {:type "text/css"
    :rel "stylesheet"
    :href href}])

(def landing-page
  [:div.h-screen.flex

   ;; -- Nav
   [:nav.bg-gray-100.flex.flex-col.pt-8.px-6.border-r {:class "w-1/6"}

    [:div.mb-6
     [:div.flex.flex-col
      [:a.self-start.hover:text-black.font-medium.pl-2.border-l-2
       {:class "border-transparent text-gray-600"}

       [:span "Welcome"]]]]]])

(defn index [system _]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (let [asset-prefix-url (system/site-asset-prefix-url system)]
     (page/html5
       {:lang "en"}

       [:meta
        {:name "viewport"
         :content "width=device-width, initial-scale=1.0"}]

       [:meta
        {:name "description"
         :content "Convex is an open, decentralised, and efficient technology platform built in the spirit of the original Internet."}]

       ;; -- Google Analytics

       [:script {:async "true" :src "https://www.googletagmanager.com/gtag/js?id=UA-179518463-1"}]

       [:script
        "window.dataLayer = window.dataLayer || [];
         function gtag(){dataLayer.push(arguments);}
         gtag('js', new Date());

         gtag('config', 'UA-179518463-1', { send_page_view: false });"]

       ;; -- End Google Analytics


       (stylesheet "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap")
       (stylesheet "https://fonts.googleapis.com/css2?family=Space+Mono:wght@400;700&display=swap")

       (stylesheet (str asset-prefix-url "/css/styles.css"))
       (stylesheet (str asset-prefix-url "/css/highlight/idea.css"))
       (stylesheet (str asset-prefix-url "/css/codemirror.css"))
       (stylesheet (str asset-prefix-url "/css/tippy.css"))
       (stylesheet (str asset-prefix-url "/css/spinner.css"))
       (stylesheet (str asset-prefix-url "/css/react-resizable.css"))

       [:title "Convex"]

       [:body
        (ring.util.anti-forgery/anti-forgery-field)

        [:div#app]

        (page/include-js (str asset-prefix-url "/js/main.js"))]))})

(defn json-key-fn [x]
  (cond
    (ident? x)
    (name x)

    (string? x)
    x

    :else
    (pr-str x)))

(defn json-encode [x]
  (json/write-str x :key-fn json-key-fn))

(defn json-decode [^InputStream x & [{:keys [key-fn]}]]
  (when x
    (json/read-str (slurp x) :key-fn (or key-fn keyword))))

(def handler-exception-message
  "Handler raised an Exception.")

(defn error [message & [data]]
  {:error (merge {:message message}
                 (when data
                   {:data data}))})

;; ---

(def error-code-NOBODY "NOBODY")
(def error-code-MISSING "MISSING")
(def error-code-INCORRECT "INCORRECT")
(def error-code-FORBIDDEN "FORBIDDEN")

(def error-source-server "Server")
(def error-source-cvm "CVM")
(def error-source-peer "Peer")

(defn error-body
  "Code is one of:
  - error-code-NOBODY
  - error-code-MISSING
  - error-code-INCORRECT
  - error-code-FORBIDDEN

  Source is one of:
  - error-source-server
  - error-source-cvm
  - error-source-peer"
  ([{:keys [code value source]}]
   (error-body code value source))
  ([code value source]
   {:errorCode code
    :value value
    :source source}))

(defn anomaly-incorrect [error-body]
  {::anomalies/category ::anomalies/incorrect
   ::error-body error-body})

(defn anomaly-forbidden [error-body]
  {::anomalies/category ::anomalies/forbidden
   ::error-body error-body})

(defn anomaly-not-found [error-body]
  {::anomalies/category ::anomalies/not-found
   ::error-body error-body})

;; ---

(def -server-error-response
  {:status 500
   :headers {"Content-Type" "application/transit+json"}
   :body (encoding/transit-encode (error "Sorry. Our server failed to process your request."))})

(def server-error-response
  {:status 500
   :headers {"Content-Type" "application/json"}
   :body (json-encode (error-body "ERROR" "Sorry. Our server failed to process your request." error-source-server))})

(defn server-error-response2 [body]
  {:status 500
   :headers {"Content-Type" "application/json"}
   :body (json-encode body)})

;; ---

(defn -successful-response [body & more]
  (let [response {:status 200
                  :headers {"Content-Type" "application/transit+json"}
                  :body (encoding/transit-encode body)}]
    (apply merge response more)))

(defn successful-response [body & more]
  (let [response {:status 200
                  :headers {"Content-Type" "application/json"}
                  :body (json-encode body)}]
    (apply merge response more)))

;; ---

(defn -bad-request-response [body]
  {:status 400
   :headers {"Content-Type" "application/transit+json"}
   :body (encoding/transit-encode body)})

(defn bad-request-response [body]
  {:status 400
   :headers {"Content-Type" "application/json"}
   :body (json-encode body)})

;; ---

(defn -forbidden-response [body]
  {:status 403
   :headers {"Content-Type" "application/transit+json"}
   :body (encoding/transit-encode body)})

(defn forbidden-response [body]
  {:status 403
   :headers {"Content-Type" "application/json"}
   :body (json-encode body)})

;; ---

(defn -not-found-response [body]
  {:status 404
   :headers {"Content-Type" "application/transit+json"}
   :body (encoding/transit-encode body)})

(defn not-found-response [body]
  {:status 404
   :headers {"Content-Type" "application/json"}
   :body (json-encode body)})

;; ---

(defn service-unavailable-response [body]
  {:status 503
   :headers {"Content-Type" "application/json"}
   :body (json-encode body)})


(defn parse-lang [lang]
  ({:convexLisp :convex-lisp
    :convex-lisp :convex-lisp} (or (some-> lang keyword) :convex-lisp)))


;; Public APIs
;; ==========================

(defn GET-v1-account [context address]
  (let [peer (system/convex-peer context)

        address (try
                  (convex/address address)
                  (catch Exception ex
                    (throw (ex-info "Invalid Address."
                                    (anomaly-incorrect (error-body error-code-INCORRECT (ex-message ex) error-source-server))))))

        account-status (convex/account-status peer address)]
    (if-let [account-status-data (convex/account-status-data account-status)]
      (successful-response (merge {:address (.longValue address)} (rename-keys account-status-data {:convex-web.account-status/actor? :isActor
                                                                                                    :convex-web.account-status/library? :isLibrary
                                                                                                    :convex-web.account-status/memory-size :memorySize
                                                                                                    :convex-web.account-status/account-key :accountKey})))
      (let [message (str "The Account for this Address does not exist.")]
        (throw (ex-info message
                        (anomaly-not-found (error-body error-code-NOBODY message error-source-server))))))))

(defn POST-v1-transaction-prepare [system {:keys [body]}]
  (let [{:keys [address source lang sequence] :as prepare} (json-decode body)

        _ (log/debug "Prepare transaction" prepare)

        lang (parse-lang lang)

        _ (when-not (contains? #{:convex-lisp} lang)
            (throw (ex-info "Incorrect lang."
                            (anomaly-incorrect
                              (error-body error-code-INCORRECT
                                          "Incorrect lang."
                                          error-source-server)))))

        _ (when-not (s/valid? :convex-web/non-empty-string source)
            (throw (ex-info "Source is required."
                            (anomaly-incorrect
                              (error-body error-code-MISSING
                                          "Source is required."
                                          error-source-server)))))

        address (try
                  (convex/address address)
                  (catch Exception _
                    (throw (ex-info (str "Invalid address: " address)
                                    (anomaly-incorrect
                                      (error-body error-code-INCORRECT
                                                  (str "Invalid address: " address)
                                                  error-source-server))))))]
    (locking (convex/lockee address)
      (let [peer (system/convex-peer system)

            next-sequence-number (or sequence (inc (or (convex/get-sequence-number address)
                                                       (convex/sequence-number peer address)
                                                       0)))

            tx (convex/invoke-transaction {:nonce next-sequence-number
                                           :address address
                                           :command (convex/read-source source)})

            tx-ref (.toHexString (.getHash tx))]

        (convex/set-sequence-number! address next-sequence-number)

        ;; Persist the transaction in the Etch datastore.
        (ACell/createPersisted tx)

        (log/debug "Persisted transaction ref" tx-ref)

        (successful-response {:sequence next-sequence-number
                              :address (.longValue address)
                              :source source
                              :lang lang
                              :hash tx-ref})))))

(defn POST-v1-transaction-submit [system {:keys [body]}]
  (let [{:keys [address sig hash accountKey] :as body} (json-decode body)

        _ (log/debug "Submit transaction" body)

        address (try
                  (convex/address address)
                  (catch Exception _
                    (throw (ex-info (str "Invalid address: " address)
                             (anomaly-incorrect
                               (error-body error-code-INCORRECT
                                 (str "Invalid address: " address)
                                 error-source-server))))))

        _ (when-not (s/valid? :convex-web/non-empty-string hash)
            (throw (ex-info "Invalid hash."
                     (anomaly-incorrect
                       (error-body error-code-INCORRECT
                         "Invalid hash."
                         error-source-server)))))

        _ (when-not (s/valid? :convex-web/sig sig)
            (throw (ex-info "Invalid signature."
                     (anomaly-incorrect
                       (error-body error-code-INCORRECT
                         "Invalid signature."
                         error-source-server)))))

        sig (ASignature/fromHex sig)

        tx-ref (Ref/forHash (Hash/fromHex hash))

        _ (log/debug (str "Ref for hash " hash) tx-ref)

        accountKey (AccountKey/fromHex accountKey)

        signed-data (SignedData/create accountKey sig tx-ref)

        _ (when-not (.checkSignature signed-data)
            (throw (ex-info "Invalid signature."
                     (anomaly-forbidden
                       (error-body error-code-FORBIDDEN
                         "Invalid signature."
                         error-source-peer)))))

        ;; Get value to check for a missing exception.
        _ (try
            (.getValue signed-data)
            (catch MissingDataException ex
              (throw (ex-info (ex-message ex)
                       (anomaly-not-found
                         (error-body error-code-MISSING
                           (ex-message ex)
                           error-source-peer))))))

        client (system/convex-client system)

        _ (log/debug "Transact signed data" signed-data)

        result (try
                 (convex/transact-signed client signed-data)
                 (catch ExceptionInfo ex
                   ;; Reset sequence number for Address, because we don't know the Peer's state.
                   (convex/reset-sequence-number! address)

                   (throw (ex-info (ex-message ex)
                            (anomaly-incorrect
                              (error-body error-code-INCORRECT
                                (ex-message ex)
                                error-source-cvm))))))

        bad-sequence-number? (when-let [error-code (.getErrorCode result)]
                               (= :SEQUENCE (convex/datafy error-code)))

        ;; Reset sequence number for Address, if we got it wrong.
        _ (when bad-sequence-number?
            (log/error "Result error: Bad sequence number.")

            (convex/reset-sequence-number! address))

        result-value (.getValue result)

        result-response (merge {:value
                                (try
                                  (convex/datafy result-value)
                                  (catch Exception ex
                                    (log/warn ex "Can't datafy Transaction result. Will fallback to `(str result)`.")
                                    (str result-value)))}
                          (when-let [error-code (.getErrorCode result)]
                            {:errorCode (convex/datafy-safe error-code)
                             :source error-source-cvm}))

        _ (log/debug "Transaction result" result)]

    (successful-response result-response)))

(defn POST-v1-create-account [system {:keys [body]}]
  (let [{:keys [accountKey]} (json-decode body)]

    (when (str/blank? accountKey)
      (throw (ex-info "Missing account key."
               (anomaly-incorrect
                 (error-body "MISSING" "Missing account key." error-source-server)))))

    (let [^AccountKey account-key (try
                                    (convex/account-key-from-hex accountKey)
                                    (catch Throwable ex
                                      (throw (ex-info (ex-message ex)
                                               (anomaly-incorrect
                                                 (error-body {:code error-code-INCORRECT
                                                              :value (ex-message ex)
                                                              :source error-source-server}))))))

          client (system/convex-client system)

          ^Address genesis-address (convex/genesis-address)

          generated-address (try
                              (convex/create-account client genesis-address account-key)
                              (catch ExceptionInfo ex
                                (let [{:keys [result] ::anomalies/keys [category]} (ex-data ex)

                                      error-code (or (some-> ^Result result
                                                       (.getErrorCode)
                                                       (convex/datafy-safe))
                                                   error-code-INCORRECT)

                                      error-message (ex-message ex)

                                      error-source (cond
                                                     (= ::anomalies/incorrect category)
                                                     error-source-server

                                                     :else
                                                     error-source-cvm)]
                                  (throw
                                    (ex-info error-message
                                      (anomaly-incorrect
                                        (error-body error-code error-message error-source)))))))]

      (successful-response {:address (.longValue generated-address)}))))

(defn POST-v1-faucet [system {:keys [body]}]
  (let [{:keys [address amount]} (json-decode body)

        address (try
                  (convex/address address)
                  (catch Exception _
                    (throw (ex-info (str "Invalid address: " address)
                             (anomaly-incorrect
                               (error-body error-code-INCORRECT
                                 (str "Invalid address: " address)
                                 error-source-server))))))]


    (cond
      (not (s/valid? :convex-web/amount amount))
      (throw (ex-info (str "Invalid amount: " amount)
               (anomaly-incorrect
                 (error-body error-code-INCORRECT
                   (str "Invalid amount: " amount)
                   error-source-server))))

      (> amount config/max-faucet-amount)
      (let [message (str "You can't request more than " (pprint/cl-format nil "~:d" config/max-faucet-amount) ".")]
        (throw (ex-info message
                 (anomaly-incorrect
                   (error-body error-code-INCORRECT
                     message
                     error-source-server)))))

      :else
      (let [client (system/convex-client system)

            ^Address genesis-address (convex/genesis-address)

            transfer (convex/transfer-transaction {:address genesis-address
                                                   :nonce 0
                                                   :target address
                                                   :amount amount})

            result (try
                     (convex/transact client transfer)
                     (catch Exception ex
                       (throw (ex-info (ex-message ex)
                                (anomaly-incorrect
                                  (error-body error-code-INCORRECT
                                    (ex-message ex)
                                    error-source-cvm))))))

            result-value (.getValue result)

            result-response (merge {:value
                                    (try
                                      (convex/datafy result-value)
                                      (catch Exception ex
                                        (log/warn ex "Can't datafy Faucet result. Will fallback to `(str result)`.")
                                        (str result-value)))}
                              (when-let [error-code (.getErrorCode result)]
                                {:error-code (convex/datafy error-code)}))

            faucet (merge {:address (convex/datafy address) :amount amount} result-response)]

        (u/log :logging.event/faucet
          :severity :info
          :target address
          :amount amount)

        (successful-response faucet)))))

(defn POST-v1-query [system {:keys [body]}]
  (let [{:keys [address source lang]} (json-decode body)

        lang (parse-lang lang)

        _ (u/log :logging.event/query
                 :severity :info
                 :address address
                 :source source
                 :lang lang)

        _ (when-not (contains? #{:convex-lisp} lang)
            (throw (ex-info "Invalid lang."
                            (anomaly-incorrect
                              (error-body error-code-INCORRECT
                                          "Invalid lang."
                                          error-source-server)))))

        address (try
                  (convex/address address)
                  (catch Exception _
                    (throw (ex-info (str "Invalid address: " address)
                                    (anomaly-incorrect
                                      (error-body error-code-INCORRECT
                                                  (str "Invalid address: " address)
                                                  error-source-server))))))

        _ (when-not (s/valid? :convex-web/non-empty-string source)
            (throw (ex-info "Source is required."
                            (anomaly-incorrect
                              (error-body error-code-MISSING
                                          "Source is required."
                                          error-source-server)))))

        form (try
               (convex/read-source source)
               (catch ExceptionInfo ex
                 (throw (ex-info (ex-message ex)
                                 (anomaly-incorrect
                                   (error-body error-code-INCORRECT
                                               (ex-message ex)
                                               error-source-server))))))

        result (convex/execute-query (system/convex-peer system) form {:address address})

        result-response (merge {:value
                                (try
                                  (convex/datafy result)
                                  (catch Exception ex
                                    (log/warn ex "Can't datafy Query result. Will fallback to `(str result)`.")
                                    (str result)))}
                               (when (instance? AExceptional result)
                                 {:errorCode (convex/datafy (.getCode result))
                                  :source error-source-cvm}))]

    (successful-response result-response)))


;; Internal APIs
;; ==========================

(defn -GET-commands [system _]
  (try
    (-successful-response (command/find-all (system/db system)))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -GET-command-by-id [system id]
  (try
    (if-let [command (command/find-by-id (system/db system) id)]
      (-successful-response (command/prune command))
      (-not-found-response {:error {:message (str "Command " id " not found.")}}))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -POST-command [system {:keys [body] :as request}]
  (try
    (let [{::command/keys [signer mode] :as command} (encoding/transit-decode body)

          sid (ring-session request)

          {signer-address :convex-web.account/address} signer

          signer (session/find-account (system/db system)
                   {:sid sid
                    :address signer-address})

          ;; Signer is the account holding the key pair to sign the query/transaction.
          command (merge command (when signer
                                   {:convex-web.command/signer signer}))

          invalid? (not (s/valid? :convex-web/command command))

          forbidden? (case mode
                       :convex-web.command.mode/query
                       false

                       :convex-web.command.mode/transaction
                       (nil? signer)

                       false)]

      (cond
        forbidden?
        (let [error (error "Unauthorized.")]

          (log/error "Command error." error)

          (-forbidden-response error))

        invalid?
        (let [error (error (str "Invalid Command.\n"
                             (expound/expound-str :convex-web/command command)))]

          (log/error "Command error." error)

          (-bad-request-response error))

        :else
        (-successful-response (command/execute system command))))
    (catch Throwable ex
      (log/error ex "Command error.")

      -server-error-response)))

(defn -POST-create-account
  "Ring Handler to create a new account.

  Internal API."
  [system _]
  (try
    (let [^Convex client (system/convex-client system)

          ^Address genesis-address (convex/genesis-address)

          ^AKeyPair generated-key-pair (AKeyPair/generate)

          ^AccountKey account-key (.getAccountKey generated-key-pair)

          ^Address generated-address (convex/create-account client genesis-address account-key)

          account #:convex-web.account {:address (.longValue generated-address)
                                        :created-at (inst-ms (Instant/now))
                                        :key-pair (convex/key-pair-data generated-key-pair)}]

      ;; Accounts created on Convex Web are persisted into the database.
      ;; NOTE: Not all Accounts in Convex are persisted in the Convex Web database.
      (d/transact! (system/db-conn system) [account])

      (-successful-response (select-keys account [::account/address
                                                  ::account/created-at])))
    (catch Exception _
      -server-error-response)))

(defn -POST-confirm-account
  "Ring Handler to confirm an account.

  Internal API."
  [system {:keys [body] :as req}]
  (try
    (let [^Long address-long (encoding/transit-decode body)

          account (account/find-by-address (system/db system) address-long)]
      (cond
        (nil? account)
        (-not-found-response (error (str "Account " address-long " not found.")))

        :else
        (let [^Convex client (system/convex-client system)

              ^Address genesis-address (convex/genesis-address)

              tx-data {:nonce 0
                       :address genesis-address
                       :target address-long
                       :amount 100000000}

              result (->> (convex/transfer-transaction tx-data)
                       (convex/transact client))]

          (if (.isError result)
            (throw (ex-info "Failed to transfer funds." {:error-code (.getErrorCode result)}))
            (let [sid (ring-session req)

                  wallet-account (select-keys account [::account/address
                                                       ::account/key-pair])

                  session (if-let [session (session/find-session (system/db system) sid)]
                            ;; Update an existing session.
                            (update session :convex-web.session/wallet (fnil conj #{}) wallet-account)

                            ;; Else; create a new session.
                            {:convex-web.session/id sid
                             :convex-web.session/wallet #{wallet-account}})]

              (d/transact! (system/db-conn system) [session])

              (-successful-response wallet-account))))))
    (catch Exception ex
      (log/error ex "Account confirmation error.")

      -server-error-response)))

(defn -POST-faucet [context {:keys [body]}]
  (try
    (let [{:convex-web.faucet/keys [target amount] :as faucet} (encoding/transit-decode body)

          account (account/find-by-address (system/db context) target)

          [last-faucet & _] (sort-by :convex-web.faucet/timestamp #(compare %2 %1) (get account ::account/faucets))
          last-faucet-timestamp (get last-faucet :convex-web.faucet/timestamp 0)
          last-faucet-millis-ago (- (.getTime (Date.)) last-faucet-timestamp)]

      (cond
        (not (s/valid? :convex-web/faucet faucet))
        (let [message "Invalid request."]
          (u/log :logging.event/user-error
            :severity :error
            :message (str message " Expound: " (expound/expound-str :convex-web/faucet faucet)))
          (-bad-request-response (error message)))

        (< last-faucet-millis-ago config/faucet-wait-millis)
        (let [message (str "You need to wait "
                        (-> config/faucet-wait-millis
                          (/ 1000)
                          (/ 60))
                        " minutes to submit a new request.")]
          (u/log :logging.event/user-error
            :severity :error
            :message message)
          (-bad-request-response (error message)))

        (> amount config/max-faucet-amount)
        (let [message (str "You can't request more than " (pprint/cl-format nil "~:d" config/max-faucet-amount) ".")]
          (u/log :logging.event/user-error
            :severity :error
            :message message)
          (-bad-request-response (error message)))

        :else
        (let [client (system/convex-client context)

              ^Address genesis-address (convex/genesis-address)

              result (convex/faucet client {:address genesis-address
                                            :target target
                                            :amount amount})

              faucet {:convex-web.faucet/id (convex/datafy (.getID result))
                      :convex-web.faucet/target target
                      :convex-web.faucet/amount amount
                      :convex-web.faucet/timestamp (.getTime (Date.))}]

          (d/transact! (system/db-conn context) [{::account/address target
                                                  ::account/faucets faucet}])

          (u/log :logging.event/faucet
            :severity :info
            :target target
            :amount amount)

          (-successful-response faucet))))

    (catch Exception ex
      (u/log :logging.event/system-error
        :severity :error
        :message handler-exception-message
        :exception ex)

      -server-error-response)))

(defn -GET-session [system req]
  (try
    (let [id (ring-session req)
          session (merge {::session/id id} (session/find-session (system/db system) id))]
      (-successful-response session))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -GET-STATE [system _]
  (try
    (let [^Peer peer (system/convex-peer system)
          ^State state (convex/consensus-state peer)]
      (-successful-response
        #:convex-web.state {:accounts-count (.count (.getAccounts state))
                            :peers-count (.count (.getPeers state))
                            :memory-size (.getMemorySize state)
                            :schedule-count (.count (.getSchedule state))
                            :globals (convex/datafy (.getGlobals state))}))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -GET-accounts [context {:keys [query-params]}]
  (try
    (let [{:strs [start end]} query-params

          ^Peer peer (system/convex-peer context)

          number-of-accounts (.size (.getAccounts (.getConsensusState peer)))
          number-of-items (min number-of-accounts config/default-range)

          end (or (some-> end Long/parseLong) number-of-items)
          start (or (some-> start Long/parseLong) 0)

          start-valid? (<= 0 start end)
          end-valid? (>= number-of-accounts end start)
          range-valid? (<= (- end start) config/max-range)]
      (cond
        (not start-valid?)
        (let [message (str "Invalid start: " start ".")]
          (log/error (str "Failed to get Accounts; " message))
          (-bad-request-response (error message)))

        (not end-valid?)
        (let [message (str "Invalid end: " end ".")]
          (log/error (str "Failed to get Accounts; " message))
          (-bad-request-response (error message)))

        (not range-valid?)
        (let [message (str "Invalid range: [" start ":" end "].")]
          (log/error (str "Failed to get Accounts; " message))
          (-bad-request-response (error message)))

        :else
        (-successful-response {:meta
                               {:start start
                                :end end
                                :total number-of-accounts}

                               :convex-web/accounts
                               (convex/ranged-accounts peer {:start start :end end})})))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -GET-account [system address]
  (try
    (let [address (convex/address-safe address)

          peer (system/convex-peer system)

          account-status (try
                           (convex/account-status peer address)
                           (catch Throwable ex
                             (u/log :logging.event/system-error
                               :message (str "Failed to read Account Status " address ". Exception:" ex)
                               :exception ex)

                             nil))

          account-status-data (convex/account-status-data account-status)

          ;; Environment is lazily loaded.
          account-status-data (update account-status-data :convex-web.account-status/environment
                                (fn [env]
                                  (->> env
                                    (map
                                      (fn [[k _]]
                                        [k (with-meta {} {:convex-web/lazy? true})]))
                                    (into {}))))]
      (if account-status
        (let [form (convex/read-source (str "(call *registry* (lookup " address "))"))

              ;; It's possible to register metadata for accounts in the *registry*.
              registry (convex/execute-query (system/convex-peer system) form {:address address})
              registry (convex/datafy registry)]

          (-successful-response (merge #:convex-web.account {:address (.longValue address)
                                                             :status account-status-data}
                                  (when registry
                                    {:convex-web.account/registry registry}))))
        (let [message (str "The Account for this Address does not exist.")]
          (log/error message address)
          (-not-found-response {:error {:message message}}))))
    (catch Throwable ex
      (log/error ex (str "Get account error. Address: " address))

      -server-error-response)))

(defn -POST-env
  "Returns value bound to symbol `sym` in the environment for account `address`."
  [context {:keys [body]}]
  (try
    (let [{:keys [address sym]} (encoding/transit-decode body)

          address (convex/address-safe address)

          peer (system/convex-peer context)

          account-status (convex/account-status peer address)]

      (if-let [account-status-data (convex/account-status-data account-status)]
        (-successful-response (get-in account-status-data [:convex-web.account-status/environment sym]))
        (-not-found-response {:error {:message "Can't find symbol."}})))

    (catch Throwable ex
      (log/error ex "Get env error.")

      -server-error-response)))

(defn -POST-wallet-account-key-pair
  "Returns value bound to symbol `sym` in the environment for account `address`."
  [system req]
  (try
    (let [{:keys [body]} req

          {:keys [address]} (encoding/transit-decode body)]

      (if-let [kp (wallet/account-key-pair (system/db system)
                    {:convex-web.session/id (ring-session req)
                     :convex-web/address address})]
        (-successful-response kp)
        (-not-found-response {:error {:message "Not found."}})))

    (catch Throwable ex
      (log/error ex "Get account key pair error.")

      -server-error-response)))

(defn -GET-blocks-range [context {:keys [query-params]}]
  (try
    (let [{:strs [start end]} query-params

          peer (system/convex-peer context)
          order (convex/peer-order peer)
          consensus (convex/consensus-point order)

          max-items (min consensus config/default-range)

          end (or (some-> end Long/parseLong) consensus)
          end (min end consensus)

          start (or (some-> start Long/parseLong) (- end max-items))

          start-valid? (<= 0 start end)
          end-valid? (>= consensus end start)
          range-valid? (<= (- end start) config/max-range)]
      (cond
        (not start-valid?)
        (-bad-request-response (error (str "Invalid start: " start ".")))

        (not end-valid?)
        (-bad-request-response (error (str "Invalid end: " end ".")))

        (not range-valid?)
        (-bad-request-response (error (str "Invalid range: [" start ":" end "].")))

        :else
        (let [blocks (convex/blocks-data peer {:start start :end end})
              blocks (map
                       (fn [block]
                         (let [txs (map
                                     (fn [tx]
                                       (assoc-in tx [:convex-web.signed-data/value :convex-web.transaction/result] (with-meta {} {:convex-web/lazy? true})))
                                     (get block :convex-web.block/transactions))]

                           (assoc block :convex-web.block/transactions txs)))
                       blocks)]
          (-successful-response {:meta
                                 {:start start
                                  :end end
                                  :total consensus}

                                 :convex-web/blocks
                                 blocks}))))
    (catch Exception ex
      (log/error ex "Failed to get blocks.")

      -server-error-response)))

(defn -GET-block [context index]
  (try
    (let [index (Long/parseLong index)

          peer (system/convex-peer context)

          ^Order order (convex/peer-order peer)]

      (if (<= 0 index (dec (.getBlockCount order)))
        (-successful-response (convex/block-data peer index (.getBlock order index)))
        (-not-found-response {:error {:message (str "Block " index " doesn't exist.")}})))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      (log/error ex (str "Failed to get Block " index))

      -server-error-response)))

(defn -GET-reference [context _]
  (try
    (let [^Context server-context (system/convex-world-context context)]
      (-successful-response
        (convex/library-reference server-context)))
    (catch Exception ex
      (u/log :logging.event/system-error
        :severity :error
        :message handler-exception-message
        :exception ex)

      -server-error-response)))

(defn -GET-markdown-page [_ request]
  (try
    (let [page (get-in request [:query-params "page"])
          markdown-page (read-markdown-page (keyword page))]
      (cond
        (nil? markdown-page)
        (-not-found-response (error "Markdown page not found."))

        :else
        (-successful-response markdown-page)))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defmulti invoke*
  (fn [_ _ body]
    (:convex-web.invoke/id body)))

(defmethod invoke* :convex-web.invoke/wallet-account-key-pair
  [system req body]
  (let [address (get-in body [:convex-web.invoke/body :address])]
    (if-let [kp (wallet/account-key-pair (system/db system)
                  {:convex-web.session/id (ring-session req)
                   :convex-web/address address})]
      (-successful-response kp)
      (-not-found-response {:error {:message "Not found."}}))))

(defmethod invoke* :convex-web.invoke/wallet-add-account
  [system req body]
  (let [{invoke-body :convex-web.invoke/body} body

        {req-address :address
         req-seed :seed
         req-account-key :account-key
         req-private-key :private-key} invoke-body

        req-address (convex/address req-address)

        ;; It's possible to recreate the key pair from seed.
        key-pair-from-seed (when req-seed
                             (convex/key-pair-data
                               (Ed25519KeyPair/create
                                 (Blob/fromHex (str/replace req-seed #"^0x" "")))))

        key-pair-account-key (when req-account-key
                               {:convex-web.key-pair/account-key (str/replace req-account-key #"^0x" "")})

        key-pair-private-key (when req-private-key
                               {:convex-web.key-pair/private-key (str/replace req-private-key #"^0x" "")})

        key-pair-from-keys (merge key-pair-account-key key-pair-private-key)

        key-pair (or key-pair-from-seed key-pair-from-keys)

        to-be-added-account (merge {:convex-web.account/address (.longValue req-address)}
                              (when key-pair
                                {:convex-web.account/key-pair key-pair}))

        sid (ring-session req)

        session (if-let [session (session/find-session (system/db system) sid)]
                  ;; Update an existing session.
                  (update session :convex-web.session/wallet (fnil conj #{}) to-be-added-account)

                  ;; Else; Create a new session.
                  {:convex-web.session/id sid
                   :convex-web.session/wallet #{to-be-added-account}})

        session (select-keys session [:convex-web.session/id :convex-web.session/wallet])

        {db :db-after} (d/transact! (system/db-conn system) [session])]

    (-successful-response (session/find-session db (ring-session req)))))

(defmethod invoke* :convex-web.invoke/wallet-remove-account
  [system req body]
  (let [{invoke-body :convex-web.invoke/body} body

        {address-to-be-removed :address} invoke-body

        sid (ring-session req)

        session (session/find-session (system/db system) sid)

        {wallet :convex-web.session/wallet} session

        wallet (reduce
                 (fn [wallet account]
                   (let [{address-in-wallet :convex-web.account/address} account]
                     (if (not= (convex/address address-to-be-removed)
                           (convex/address-safe address-in-wallet))
                       (conj wallet account)
                       wallet)))
                 #{}
                 wallet)

        {db :db-after} (d/transact! (system/db-conn system) [{:convex-web.session/id sid
                                                              :convex-web.session/wallet wallet}])]

    (-successful-response (session/find-session db (ring-session req)))))

(defn invoke
  "Internal invoke API.

  Invoke a Ring handler registered by ID."
  [system req]
  (try

    (let [{body-encoded :body} req

          body (encoding/transit-decode body-encoded)]

      (invoke* system req body))

    (catch Throwable ex
      (log/error ex "Invoke error.")

      {:status 500
       :headers {"Content-Type" "application/transit+json"}
       :body (encoding/transit-encode (error (ex-message ex)))})))

(defn site [system]
  (routes
    (GET "/api/internal/session" req (-GET-session system req))
    (POST "/api/internal/generate-account" req (-POST-create-account system req))
    (POST "/api/internal/confirm-account" req (-POST-confirm-account system req))
    (POST "/api/internal/faucet" req (-POST-faucet system req))
    (GET "/api/internal/accounts" req (-GET-accounts system req))
    (GET "/api/internal/accounts/:address" [address] (-GET-account system address))
    (POST "/api/internal/env" req (-POST-env system req))
    (GET "/api/internal/blocks-range" req (-GET-blocks-range system req))
    (GET "/api/internal/blocks/:index" [index] (-GET-block system index))
    (GET "/api/internal/commands" req (-GET-commands system req))
    (POST "/api/internal/commands" req (-POST-command system req))
    (GET "/api/internal/commands/:id" [id] (-GET-command-by-id system (Long/parseLong id)))
    (GET "/api/internal/reference" req (-GET-reference system req))
    (GET "/api/internal/markdown-page" req (-GET-markdown-page system req))
    (GET "/api/internal/state" req (-GET-STATE system req))

    (POST "/api/internal/invoke" req (invoke system req))

    (route/resources "/")

    ;; Fallback to index and let the app router handle "not found".
    (fn index-handler [req]
      (index system req))))

(defn public-api [system]
  (routes
    (GET "/api/v1/accounts/:address" [address] (GET-v1-account system address))
    (POST "/api/v1/createAccount" req (POST-v1-create-account system req))
    (POST "/api/v1/faucet" req (POST-v1-faucet system req))
    (POST "/api/v1/query" req (POST-v1-query system req))
    (POST "/api/v1/transaction/prepare" req (POST-v1-transaction-prepare system req))
    (POST "/api/v1/transaction/submit" req (POST-v1-transaction-submit system req))))

(defn wrap-logging [handler]
  (fn wrap-logging-handler [request]
    (u/with-context
      {:logging.mdc/http-request request}
      (let [response (handler request)]
        (u/log :logging.event/endpoint :logging.mdc/http-response response)

        response))))

(defn wrap-error [handler]
  (fn wrap-error-handler [request]
    (try
      (handler request)
      (catch Throwable ex
        (log/error "Web handler exception:" (with-out-str (stacktrace/print-stack-trace ex)))

        ;; Mapping of anomalies category to HTTP status code.
        (let [{::keys [error-body] ::anomalies/keys [category]} (ex-data ex)]
          (case category
            ::anomalies/not-found
            (not-found-response error-body)

            ::anomalies/forbidden
            (forbidden-response error-body)

            ::anomalies/incorrect
            (bad-request-response error-body)

            ::anomalies/busy
            (service-unavailable-response error-body)

            ;; NOTE
            ;; Disclose error details to the client for debugging purposes - for the time being (2021-02-10).

            ::anomalies/fault
            (server-error-response2
              (convex-web.web-server/error-body "SERVER"
                (with-out-str (stacktrace/print-stack-trace ex))
                error-source-server))

            ;; Default
            (convex-web.web-server/server-error-response2
              (convex-web.web-server/error-body "SERVER"
                (with-out-str (stacktrace/print-stack-trace ex))
                error-source-server))))))))

(defn public-api-handler [system]
  (-> (public-api system)
      (wrap-error)
      (wrap-logging)
      (wrap-defaults api-defaults)
      (wrap-cors :access-control-allow-origin #".*"
                 :access-control-allow-methods [:get :put :post :delete])))

(defn site-handler [system]
  (let [now-plus-1-year (.plusYears (ZonedDateTime/now) 1)

        ;; The maximum lifetime of the cookie as an HTTP-date timestamp
        ;;
        ;; If unspecified, the cookie becomes a session cookie.
        ;; A session finishes when the client shuts down, and session cookies will be removed.
        ;;
        ;; See:
        ;;  https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie
        ;;  https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Date
        session-expires (.format now-plus-1-year DateTimeFormatter/RFC_1123_DATE_TIME)

        site-config (merge {:session
                            {:store (session/persistent-session-store (system/db-conn system))
                             :flash true
                             :cookie-attrs
                             {:http-only false
                              :same-site :strict
                              :expires session-expires}}}
                      (system/site-config system))]
    (-> (site system)
      (wrap-logging)
      (wrap-defaults (merge-with merge site-defaults site-config)))))

(defn run-server
  "Start HTTP server (default port is 8090).

   Returns `(fn [& {:keys [timeout] :or {timeout 100}}])`
   which you can call to stop the server.

   `options` are the same as org.httpkit.server/run-server."
  [system & [options]]
  (let [;; The public API handler must come first
        ;; because the Site handler returns the index if a route is not found.
        handler (routes (public-api-handler system) (site-handler system))]

    (http-kit/run-server handler options)))

