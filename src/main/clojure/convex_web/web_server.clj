(ns convex-web.web-server
  (:require [convex-web.specs]
            [convex-web.convex :as convex]
            [convex-web.peer :as peer]
            [convex-web.system :as system]
            [convex-web.account :as account]
            [convex-web.session :as session]
            [convex-web.command :as command]
            [convex-web.config :as config]
            [convex-web.encoding :as encoding]

            [clojure.set :refer [rename-keys]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [clojure.pprint :as pprint]
            [clojure.stacktrace :as stacktrace]
            [clojure.data.json :as json]

            [cognitect.anomalies :as anomalies]

            [com.brunobonacci.mulog :as u]
            [expound.alpha :as expound]
            [datalevin.core :as d]
            [cognitect.transit :as t]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [org.httpkit.server :as http-kit]
            [compojure.core :refer [routes GET POST]]
            [compojure.route :as route]
            [hiccup.page :as page]
            [ring.util.anti-forgery])
  (:import (java.io InputStream)
           (convex.core.crypto AKeyPair Hash ASignature)
           (convex.core.data Address AccountStatus Ref SignedData)
           (convex.core Init Peer State)
           (java.time Instant)
           (java.util Date)
           (org.parboiled.errors ParserRuntimeException)
           (convex.core.exceptions ParseException MissingDataException)
           (convex.core.lang.impl AExceptional)
           (convex.api Convex)
           (convex.core.transactions Invoke)
           (java.util.concurrent TimeoutException)))

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

       ;; -- Google Analytics

       [:script {:async "true" :src "https://www.googletagmanager.com/gtag/js?id=UA-179518463-1"}]

       [:script
        "window.dataLayer = window.dataLayer || [];
         function gtag(){dataLayer.push(arguments);}
         gtag('js', new Date());

         gtag('config', 'UA-179518463-1', { send_page_view: false });"]

       ;; -- End Google Analytics


       (stylesheet "https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap")
       (stylesheet "https://fonts.googleapis.com/css2?family=Space+Mono&display=swap")

       (stylesheet (str asset-prefix-url "/css/highlight/idea.css"))
       (stylesheet (str asset-prefix-url "/css/codemirror.css"))
       (stylesheet (str asset-prefix-url "/css/styles.css"))
       (stylesheet (str asset-prefix-url "/css/tippy.css"))
       (stylesheet (str asset-prefix-url "/css/spinner.css"))

       [:title "Convex"]

       [:body
        (ring.util.anti-forgery/anti-forgery-field)

        [:div#app]

        (page/include-js (str asset-prefix-url "/js/main.js"))]))})

(defn transit-decode [^InputStream x]
  (when x
    (t/read (t/reader x :json))))

(defn json-encode [x]
  (json/write-str x))

(defn json-decode [^InputStream x & [{:keys [key-fn]}]]
  (when x
    (json/read-str (slurp x) :key-fn (or key-fn keyword))))

(def handler-exception-message
  "An unhandled exception was thrown during the handler execution.")

(defn error [message & [data]]
  {:error (merge {:message message}
                 (when data
                   {:data data}))})

(def -server-error-response
  {:status 500
   :headers {"Content-Type" "application/transit+json"}
   :body (encoding/transit-encode (error "Sorry. Our server failed to process your request."))})

(def server-error-response
  {:status 500
   :headers {"Content-Type" "application/json"}
   :body (json-encode (error "Sorry. Our server failed to process your request."))})

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

(defn -bad-request-response [body]
  {:status 400
   :headers {"Content-Type" "application/transit+json"}
   :body (encoding/transit-encode body)})

(defn bad-request-response [body]
  {:status 400
   :headers {"Content-Type" "application/json"}
   :body (json-encode body)})

(defn forbidden-response [body]
  {:status 403
   :headers {"Content-Type" "application/transit+json"}
   :body (encoding/transit-encode body)})

(defn not-found-response [body]
  {:status 404
   :headers {"Content-Type" "application/transit+json"}
   :body (encoding/transit-encode body)})

(defn service-unavailable-response [body]
  {:status 503
   :headers {"Content-Type" "application/json"}
   :body (json-encode body)})

;; Public APIs
;; ==========================

(defn GET-v1-account [context address]
  (try
    (let [peer (peer/peer (system/convex-server context))

          account-status (try
                           (convex/account-status peer address)
                           (catch Throwable ex
                             (u/log :logging.event/system-error
                                    :message (str "Failed to read Account Status " address ". Exception:" ex)
                                    :exception ex)))]
      (if-let [account-status-data (convex/account-status-data account-status)]
        (successful-response (merge {:address address} (rename-keys account-status-data {:convex-web.account-status/actor? :is_actor
                                                                                         :convex-web.account-status/library? :is_library
                                                                                         :convex-web.account-status/memory-size :memory_size})))
        (let [message (str "The Account for this Address does not exist.")]
          (log/error message address)
          (not-found-response {:error {:message message}}))))
    (catch Throwable ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      server-error-response)))

(defn POST-v1-transaction-prepare [system {:keys [body]}]
  (let [{:keys [address source lang sequence_number]} (json-decode body)

        lang (or (some-> lang keyword) :convex-lisp)

        _ (u/log :logging.event/transaction-prepare
                 :severity :info
                 :address address
                 :source source
                 :lang lang)

        _ (when-not (contains? #{:convex-scrypt :convex-lisp} lang)
            (throw (ex-info "Invalid lang."
                            {::anomalies/category ::anomalies/incorrect})))

        address (try
                  (s/assert :convex-web/address address)
                  (catch Exception _
                    (throw (ex-info "Invalid address." {::anomalies/category ::anomalies/incorrect}))))

        source (try
                 (s/assert :convex-web/non-empty-string source)
                 (catch Exception _
                   (throw (ex-info "Invalid source." {::anomalies/category ::anomalies/incorrect}))))


        peer (system/convex-peer-server system)

        address (convex/address address)

        next-sequence-number (convex/next-sequence-number! {:address address
                                                            :next sequence_number
                                                            :not-found (peer/sequence-number peer address)})

        command (peer/read source lang)
        tx (Invoke/create next-sequence-number command)]

    ;; Persist the transaction in the Etch datastore.
    (Ref/createPersisted tx)

    (successful-response {:sequence_number next-sequence-number
                          :address address
                          :source source
                          :lang lang
                          :hash (.toHexString (.getHash tx))})))

(defn POST-v1-transaction-submit [system {:keys [body]}]
  (let [{:keys [address sig hash] :as body} (json-decode body)

        _ (log/debug "POST Transaction Submit" body)

        _ (u/log :logging.event/transaction-submit
                 :severity :info
                 :address address
                 :hash hash)

        address (try
                  (s/assert :convex-web/address address)
                  (catch Exception _
                    (throw (ex-info "Invalid address."
                                    {::anomalies/category ::anomalies/incorrect}))))

        hash (try
               (s/assert :convex-web/non-empty-string hash)
               (catch Exception _
                 (throw (ex-info "Invalid hash."
                                 {::anomalies/category ::anomalies/incorrect}))))

        sig (try
              (s/assert :convex-web/sig sig)
              (catch Exception _
                (throw (ex-info "Invalid signature."
                                {::anomalies/category ::anomalies/incorrect}))))

        sig (ASignature/fromHex sig)

        tx-ref (Ref/forHash (Hash/fromHex hash))

        _ (log/debug "Tx Ref" tx-ref)

        signed-data (SignedData/create (convex/address address) sig tx-ref)

        _ (when-not (.isValid signed-data)
            (throw (ex-info "Invalid signature."
                            {::anomalies/category ::anomalies/incorrect})))

        client (system/convex-client system)

        _ (log/debug "Transact signed data" signed-data)

        result (try
                 (.transactSync client signed-data 500)
                 (catch TimeoutException ex
                   (log/error ex "Transaction timed out.")

                   (throw (ex-info "Transaction timed out." {::anomalies/category ::anomalies/busy} ex)))

                 (catch MissingDataException ex
                   (log/error ex "Failed to transact signed data" signed-data)

                   (throw (ex-info "You need to prepare the transaction before submitting." {::anomalies/category ::anomalies/incorrect} ex)))

                 (catch Exception ex
                   (log/error ex "Transaction fault.")

                   (throw (ex-info "Transaction fault." {::anomalies/category ::anomalies/fault} ex))))

        result-response (merge {:id (.getID result)
                                :value (convex/datafy (.getValue result))}
                               (when-let [error-code (.getErrorCode result)]
                                 {:error-code (convex/datafy error-code)}))

        _ (log/debug "Transaction result" result)]

    (successful-response result-response)))

(defn POST-v1-faucet [system {:keys [body]}]
  (let [{:keys [address amount]} (json-decode body)

        bad-request (fn [message]
                      (u/log :logging.event/user-error
                             :severity :error
                             :message message)

                      (bad-request-response (error message)))]
    (cond
      (not (s/valid? :convex-web/address address))
      (bad-request "Invalid address.")

      (not (s/valid? :convex-web/amount amount))
      (bad-request "Invalid amount.")

      (> amount config/max-faucet-amount)
      (bad-request (str "You can't request more than" (pprint/cl-format nil "~:d" config/max-faucet-amount) "."))

      :else
      (let [client (system/convex-client system)

            nonce (inc (convex/hero-sequence (peer/peer (system/convex-server system))))

            transfer (convex/transfer {:nonce nonce
                                       :target address
                                       :amount amount})

            result @(.transact client transfer)
            result-response (merge {:id (.getID result)
                                    :value (convex/datafy (.getValue result))}
                                   (when-let [error-code (.getErrorCode result)]
                                     {:error-code (convex/datafy error-code)}))

            faucet (merge {:address address :amount amount} result-response)]

        (u/log :logging.event/faucet
               :severity :info
               :target address
               :amount amount)

        (successful-response faucet)))))

(defn POST-v1-query [system {:keys [body]}]
  (let [{:keys [address source lang]} (json-decode body)

        lang (or (some-> lang keyword) :convex-lisp)

        _ (u/log :logging.event/query
                 :severity :info
                 :address address
                 :source source
                 :lang lang)

        _ (when-not (contains? #{:convex-scrypt :convex-lisp} lang)
            (throw (ex-info "Invalid lang."
                            {::anomalies/category ::anomalies/incorrect})))

        address (try
                  (s/assert :convex-web/address address)
                  (catch Exception _
                    (throw (ex-info "Invalid address."
                                    {::anomalies/category ::anomalies/incorrect}))))

        source (try
                 (s/assert :convex-web/non-empty-string source)
                 (catch Exception _
                   (throw (ex-info "Source can't be empty."
                                   {::anomalies/category ::anomalies/incorrect}))))

        result (peer/query (system/convex-peer-server system) {:source source
                                                               :lang lang
                                                               :address address})

        result-response (merge {:value (convex/datafy result)}
                               (when (instance? AExceptional result)
                                 {:error-code (convex/datafy (.getCode result))}))]

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
      (not-found-response {:error {:message (str "Command " id " not found.")}}))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -POST-command [context {:keys [body] :as request}]
  (try
    (let [{::command/keys [address mode] :as command} (transit-decode body)

          invalid? (not (s/valid? :convex-web/command command))

          session-addresses (session-addresses context request)

          forbidden? (case mode
                       :convex-web.command.mode/query
                       false

                       :convex-web.command.mode/transaction
                       (not (contains? session-addresses address)))]

      (cond
        invalid?
        (do
          (u/log :logging.event/user-error
                 :severity :error
                 :message "Invalid Command."
                 :exception (ex-info (str "\n" (expound/expound-str :convex-web/command command)) {}))
          (-bad-request-response (error "Invalid Command.")))

        forbidden?
        (do
          (u/log :logging.event/user-error
                 :severity :error
                 :message "Unauthorized."
                 :exception (ex-info "Unauthorized." {}))
          (forbidden-response (error "Unauthorized.")))

        :else
        (let [command' (command/execute context command)]
          (-successful-response command'))))
    (catch Throwable ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      (let [message (cond
                      (#{ParserRuntimeException ParseException} (class ex))
                      (str "Syntax error: " (.getMessage (stacktrace/root-cause ex)))

                      :else
                      "Server error.")]
        (-successful-response #:convex-web.command {:status :convex-web.command.status/error
                                                    :error {:message message}})))))

(defn -POST-generate-account [system req]
  (try
    (u/log :logging.event/new-account :severity :info)

    (let [^Peer peer (peer/peer (system/convex-server system))
          ^State state (peer/consensus-state peer)
          ^AccountStatus status (peer/account-status state Init/HERO)
          ^Long sequence (peer/account-sequence status)
          ^Convex client (system/convex-client system)
          ^AKeyPair generated-key-pair (convex/generate-account client Init/HERO_KP (inc sequence))
          ^Address address (.getAddress generated-key-pair)
          ^String address-str (.toChecksumHex address)

          account #:convex-web.account {:address address-str
                                        :created-at (inst-ms (Instant/now))
                                        :key-pair (convex/key-pair-data generated-key-pair)}]

      ;; Accounts created on Convex Web are persisted into the database.
      ;; NOTE: Not all Accounts in Convex are persisted in the Convex Web database.
      (d/transact! (system/db-conn system) [account])

      (-successful-response (select-keys account [::account/address
                                                  ::account/created-at])))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -POST-confirm-account [system {:keys [body] :as req}]
  (try
    (let [^String address-str (transit-decode body)

          account (account/find-by-address (system/db system) address-str)]
      (cond
        (nil? account)
        (do
          (u/log :logging.event/user-error :severity :error :message (str "Failed to confirm account; Account " address-str " not found."))
          (not-found-response (error (str "Account " address-str " not found."))))

        :else
        (do
          (u/log :logging.event/confirm-account
                 :severity :info
                 :address address-str
                 :message (str "Confirmed Address " address-str "."))

          (d/transact! (system/db-conn system) [{:convex-web.session/id (ring-session req)
                                                 :convex-web.session/accounts
                                                 [{:convex-web.account/address address-str}]}])

          (-successful-response (select-keys account [::account/address
                                                      ::account/created-at])))))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -POST-faucet [context {:keys [body]}]
  (try
    (let [{:convex-web.faucet/keys [target amount] :as faucet} (transit-decode body)

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

              nonce (inc (convex/hero-sequence (peer/peer (system/convex-server context))))

              result (convex/faucet client {:nonce nonce
                                            :target target
                                            :amount amount})

              faucet {:convex-web.faucet/id (.getID result)
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

(defn -GET-accounts [context {:keys [query-params]}]
  (try
    (let [{:strs [start end]} query-params

          peer (peer/peer (system/convex-server context))

          number-of-accounts (count (.getAccounts (.getConsensusState peer)))
          number-of-items (min number-of-accounts config/default-range)

          end (or (some-> end Long/parseLong) number-of-accounts)
          start (or (some-> start Long/parseLong) (- end number-of-items))

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
                               (map
                                 (fn [[address status]]
                                   (let [address (convex/address->checksum-hex address)
                                         status (convex/account-status-data status)]
                                     #:convex-web.account {:address address
                                                           ;; Dissoc `environment` because it's too much data.
                                                           :status (dissoc status :convex-web.account-status/environment)}))
                                 (convex/accounts peer {:start start :end end}))})))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -GET-account [context address]
  (try
    (let [peer (peer/peer (system/convex-server context))

          account-status (try
                           (convex/account-status peer address)
                           (catch Throwable ex
                             (u/log :logging.event/system-error
                                    :message (str "Failed to read Account Status " address ". Exception:" ex)
                                    :exception ex)))

          account-status-data (convex/account-status-data account-status)]
      (if account-status
        (-successful-response #:convex-web.account {:address address
                                                    :status account-status-data})
        (let [message (str "The Account for this Address does not exist.")]
          (log/error message address)
          (not-found-response {:error {:message message}}))))
    (catch Throwable ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -GET-blocks [context _]
  (try
    (let [peer (peer/peer (system/convex-server context))
          order (convex/peer-order peer)
          consensus (convex/consensus-point order)
          max-items (min consensus config/default-range)
          end consensus
          start (- end max-items)]
      (-successful-response (convex/blocks peer {:start start :end end})))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -GET-blocks-range [context {:keys [query-params]}]
  (try
    (let [{:strs [start end]} query-params

          peer (peer/peer (system/convex-server context))
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
        (-successful-response {:meta
                               {:start start
                                :end end
                                :total consensus}

                               :convex-web/blocks
                               (convex/blocks peer {:start start :end end})})))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -GET-block [context index]
  (try
    (let [peer (peer/peer (system/convex-server context))
          blocks-indexed (convex/blocks-indexed peer)]
      (if-let [block (get blocks-indexed (Long/parseLong index))]
        (-successful-response block)
        (not-found-response {:error {:message (str "Block " index " doesn't exist.")}})))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn -GET-reference [_ _]
  (try
    (-successful-response (convex/convex-core-reference))
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
        (not-found-response (error "Markdown page not found."))

        :else
        (-successful-response markdown-page)))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message handler-exception-message
             :exception ex)

      -server-error-response)))

(defn site [system]
  (routes
    (GET "/" req (index system req))
    (GET "/api/internal/session" req (-GET-session system req))
    (POST "/api/internal/generate-account" req (-POST-generate-account system req))
    (POST "/api/internal/confirm-account" req (-POST-confirm-account system req))
    (POST "/api/internal/faucet" req (-POST-faucet system req))
    (GET "/api/internal/accounts" req (-GET-accounts system req))
    (GET "/api/internal/accounts/:address" [address] (-GET-account system address))
    (GET "/api/internal/blocks" req (-GET-blocks system req))
    (GET "/api/internal/blocks-range" req (-GET-blocks-range system req))
    (GET "/api/internal/blocks/:index" [index] (-GET-block system index))
    (GET "/api/internal/commands" req (-GET-commands system req))
    (POST "/api/internal/commands" req (-POST-command system req))
    (GET "/api/internal/commands/:id" [id] (-GET-command-by-id system (Long/parseLong id)))
    (GET "/api/internal/reference" req (-GET-reference system req))
    (GET "/api/internal/markdown-page" req (-GET-markdown-page system req))

    (route/resources "/")
    (route/not-found "<h1>Page not found</h1>")))

(defn public-api [system]
  (routes
    (GET "/api/v1/accounts/:address" [address] (GET-v1-account system address))
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
        (log/error ex "Handler error")

        (case (get (ex-data ex) ::anomalies/category)
          ::anomalies/incorrect
          (do
            (u/log :logging.event/user-error
                   :severity :error
                   :message (ex-message ex)
                   :exception ex)

            (bad-request-response (error (ex-message ex))))

          ::anomalies/busy
          (do
            (u/log :logging.event/system-error
                   :severity :error
                   :message (ex-message ex)
                   :exception ex)

            (service-unavailable-response (error (ex-message ex))))

          ::anomalies/fault
          (do
            (u/log :logging.event/system-error
                   :severity :error
                   :message (ex-message ex)
                   :exception ex)

            server-error-response)

          (do
            (u/log :logging.event/system-error
                   :severity :error
                   :message handler-exception-message
                   :exception ex)

            server-error-response))))))

(defn public-api-handler [system]
  (-> (public-api system)
      (wrap-error)
      (wrap-logging)
      (wrap-defaults api-defaults)))

(defn site-handler [system]
  (let [site-config (merge {:session
                            {:store (session/persistent-session-store (system/db-conn system))
                             :flash true
                             :cookie-attrs {:http-only false :same-site :strict}}}
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
        ;; since the site handler matches a not found.
        handler (routes (public-api-handler system) (site-handler system))]

    (http-kit/run-server handler options)))

