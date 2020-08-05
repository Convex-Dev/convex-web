(ns convex-web.web-server
  (:require [convex-web.convex :as convex]
            [convex-web.peer :as peer]
            [convex-web.system :as system]
            [convex-web.account :as account]
            [convex-web.session :as session]
            [convex-web.command :as command]
            [convex-web.config :as config]

            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [clojure.pprint :as pprint]
            [clojure.string :as str]

            [com.brunobonacci.mulog :as u]
            [expound.alpha :as expound]
            [datascript.core :as d]
            [cognitect.transit :as t]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session.memory :as memory-session]
            [org.httpkit.server :as http-kit]
            [compojure.core :refer [routes GET POST]]
            [compojure.route :as route]
            [hiccup.page :as page]
            [nano-id.core :as nano-id]
            [ring.util.anti-forgery])
  (:import (java.io ByteArrayOutputStream InputStream)
           (convex.core.crypto AKeyPair)
           (convex.core.data Address AccountStatus)
           (convex.net Connection)
           (convex.core Init Peer State)
           (java.time Instant)
           (java.util Date)))

(def session-ref (atom {}))

(defn ring-session [request]
  (get-in request [:cookies "ring-session" :value]))

(defn session-addresses [context request]
  (let [session (session/find-session (system/db context) (ring-session request))]
    (->> (get session ::session/accounts)
         (map ::account/address)
         (into #{}))))

(defn read-markdown-page [k]
  (let [pages-by-k (edn/read-string (slurp (io/resource "markdown-pages.edn")))]
    (some->> (get pages-by-k k)
             (map
               (fn [{:keys [name path]}]
                 {:name name
                  :content (slurp (io/resource path))})))))

(defn stylesheet [href]
  [:link
   {:type "text/css"
    :rel "stylesheet"
    :href href}])

(defn index [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (page/html5
     (stylesheet "https://cdnjs.cloudflare.com/ajax/libs/material-design-iconic-font/2.2.0/css/material-design-iconic-font.min.css")
     (stylesheet "https://fonts.googleapis.com/css?family=Rubik:300")
     (stylesheet "https://fonts.googleapis.com/css2?family=Roboto:wght@300;700&display=swap")
     (stylesheet "/css/highlight/monokai.css")
     (stylesheet "/css/codemirror.css")
     (stylesheet "/css/solarized.css")
     (stylesheet "/css/styles.css")
     (stylesheet "/css/tippy.css")
     (stylesheet "/css/spinner.css")

     [:title "Convex"]

     [:body
      (ring.util.anti-forgery/anti-forgery-field)

      [:div#app]

      (page/include-js "/js/main.js")])})

(defn transit-encode [x]
  (let [out (ByteArrayOutputStream. 4096)
        writer (t/writer out :json)]
    (t/write writer x)
    (.toString out)))

(defn transit-decode [^InputStream x]
  (t/read (t/reader x :json)))

(def server-error-response
  {:status 500
   :headers {"Content-Type" "application/transit+json"}
   :body (transit-encode {:error {:message "Sorry. Our server failed to process your request."}})})

(defn error [message & [data]]
  {:error (merge {:message message}
                 (when data
                   {:data data}))})

(defn successful-response [body & more]
  (let [response {:status 200
                  :headers {"Content-Type" "application/transit+json"}
                  :body (transit-encode body)}]
    (apply merge response more)))

(defn bad-request-response [body]
  {:status 400
   :headers {"Content-Type" "application/transit+json"}
   :body (transit-encode body)})

(defn forbidden-response [body]
  {:status 403
   :headers {"Content-Type" "application/transit+json"}
   :body (transit-encode body)})

(defn not-found-response [body]
  {:status 404
   :headers {"Content-Type" "application/transit+json"}
   :body (transit-encode body)})

(defn GET-commands [context _]
  (try
    (let [datascript-conn (system/datascript-conn context)]
      (successful-response (command/query-all @datascript-conn)))
    (catch Exception ex
      (log/error ex (ex-message ex))

      server-error-response)))

(defn GET-command-by-id [context id]
  (try
    (let [datascript-conn (system/datascript-conn context)]
      (if-let [command (command/query-by-id @datascript-conn id)]
        (successful-response (-> command
                                 (command/wrap-result-metadata)
                                 (command/wrap-result)
                                 (command/prune)))
        (not-found-response {:error {:message (str "Command " id " not found.")}})))
    (catch Exception ex
      (log/error ex (ex-message ex))

      server-error-response)))

(defn POST-command [context {:keys [body] :as request}]
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
                 :message (str "Invalid Command. Expound: " (expound/expound-str :convex-web/command command)))
          (bad-request-response (error "Invalid Command.")))

        forbidden?
        (do
          (u/log :logging.event/user-error
                 :severity :error
                 :message "Unauthorized.")
          (forbidden-response (error "Unauthorized.")))

        :else
        (let [command' (command/execute context command)]
          (successful-response command'))))
    (catch Throwable ex
      (u/log :logging.event/system-error
             :severity :error
             :message (ex-message ex)
             :exception ex)

      server-error-response)))

(defn POST-generate-account [context req]
  (try
    (u/log :logging.event/new-account :severity :info)

    (let [^Peer peer (peer/peer (system/convex-server context))
          ^State state (peer/consensus-state peer)
          ^AccountStatus status (peer/account-status state Init/HERO)
          ^Long sequence (peer/account-sequence status)
          ^Connection conn (system/convex-conn context)
          ^AKeyPair generated-key-pair (convex/generate-account conn Init/HERO_KP (inc sequence))
          ^Address address (.getAddress generated-key-pair)
          ^String address-str (.toChecksumHex address)

          account #:convex-web.account {:address address-str
                                        :key-pair generated-key-pair
                                        :owner (ring-session req)
                                        :created-at (inst-ms (Instant/now))}]

      (d/transact! (system/datascript-conn context) [account])

      (successful-response (select-keys account [::account/address
                                                 ::account/owner
                                                 ::account/created-at])))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message (ex-message ex)
             :exception ex)

      server-error-response)))

(defn POST-confirm-account [context {:keys [body] :as req}]
  (try
    (let [^String address-str (transit-decode body)

          {::account/keys [owner] :as account} (account/find-by-address (system/db context) address-str)]
      (cond
        (nil? account)
        (do
          (u/log :logging.event/user-error :severity :error :message (str "Failed to confirm account; Account " address-str " not found."))
          (not-found-response {:error {:message (str "Account " address-str " not found.")}}))

        (not= owner (ring-session req))
        (do
          (u/log :logging.event/user-error :severity :error :message "Failed to confirm account; Session doesn't match Account.")
          (forbidden-response {:error {:message "You can't confirm an account which you don't own."}}))

        :else
        (do
          (u/log :logging.event/confirm-account
                 :severity :info
                 :address address-str
                 :message (str "Confirmed Address " address-str "."))

          (d/transact! (system/datascript-conn context) [{:convex-web.session/id (ring-session req)
                                                          :convex-web.session/accounts
                                                          [{:convex-web.account/address address-str}]}])

          (successful-response (select-keys account [::account/address
                                                     ::account/owner
                                                     ::account/created-at])))))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message (ex-message ex)
             :exception ex)

      server-error-response)))

(defn POST-faucet [context {:keys [body] :as request}]
  (try
    (let [{:convex-web.faucet/keys [target amount] :as faucet} (transit-decode body)

          session-addresses (session-addresses context request)

          authorized? (contains? session-addresses target)

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
          (bad-request-response (error message)))

        (not authorized?)
        (let [message "Unauthorized."]
          (u/log :logging.event/user-error
                 :severity :error
                 :message message)
          (forbidden-response (error message)))

        (< last-faucet-millis-ago config/faucet-wait-millis)
        (let [message (str "You need to wait "
                           (-> config/faucet-wait-millis
                               (/ 1000)
                               (/ 60))
                           " minutes to submit a new request.")]
          (u/log :logging.event/user-error
                 :severity :error
                 :message message)
          (bad-request-response (error message)))

        (> amount config/max-faucet-amount)
        (let [message (str "You can't request more than" (pprint/cl-format nil "~:d" config/max-faucet-amount) ".")]
          (u/log :logging.event/user-error
                 :severity :error
                 :message message)
          (bad-request-response (error message)))

        (nil? account)
        (let [message (str "Account " target " not found.")]
          (u/log :logging.event/user-error
                 :severity :error
                 :message message)
          (not-found-response (error message)))

        :else
        (let [conn (system/convex-conn context)

              nonce (inc (convex/hero-sequence (peer/peer (system/convex-server context))))

              tx-id (convex/faucet conn {:nonce nonce
                                         :target target
                                         :amount amount})

              faucet {:convex-web.faucet/id tx-id
                      :convex-web.faucet/target target
                      :convex-web.faucet/amount amount
                      :convex-web.faucet/timestamp (.getTime (Date.))}]

          (d/transact! (system/datascript-conn context) [{::account/address target
                                                          ::account/faucets faucet}])

          (u/log :logging.event/faucet
                 :severity :info
                 :target target
                 :amount amount)

          (successful-response faucet))))
    (catch Exception ex
      (u/log :logging.event/system-error
             :severity :error
             :message (ex-message ex)
             :exception ex)

      server-error-response)))

(defn GET-session [context req]
  (try
    (let [db @(system/datascript-conn context)
          id (ring-session req)
          session (merge {::session/id id} (session/find-session db id))]
      (successful-response session))
    (catch Exception ex
      (log/error ex (ex-message ex))

      server-error-response)))

(defn GET-accounts [context {:keys [query-params]}]
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
          (bad-request-response (error message)))

        (not end-valid?)
        (let [message (str "Invalid end: " end ".")]
          (log/error (str "Failed to get Accounts; " message))
          (bad-request-response (error message)))

        (not range-valid?)
        (let [message (str "Invalid range: [" start ":" end "].")]
          (log/error (str "Failed to get Accounts; " message))
          (bad-request-response (error message)))

        :else
        (successful-response {:meta
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
      (log/error ex (ex-message ex))

      server-error-response)))

(defn GET-account [context address]
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
        (successful-response #:convex-web.account {:address address
                                                   :status account-status-data})
        (let [message (str "Address " address " doesn't exist.")]
          (log/error (str "Failed to get Account; " message))
          (not-found-response {:error {:message message}}))))
    (catch Throwable ex
      (log/error ex (ex-message ex))

      server-error-response)))

(defn GET-blocks [context _]
  (try
    (let [peer (peer/peer (system/convex-server context))
          order (convex/peer-order peer)
          consensus (convex/consensus-point order)
          max-items (min consensus config/default-range)
          end consensus
          start (- end max-items)]
      (successful-response (convex/blocks peer {:start start :end end})))
    (catch Exception ex
      (log/error ex (ex-message ex))

      server-error-response)))

(defn GET-blocks-range [context {:keys [query-params]}]
  (try
    (let [{:strs [start end]} query-params

          peer (peer/peer (system/convex-server context))
          order (convex/peer-order peer)
          consensus (convex/consensus-point order)

          max-items (min consensus config/default-range)

          end (or (some-> end Long/parseLong) consensus)
          start (or (some-> start Long/parseLong) (- end max-items))

          start-valid? (<= 0 start end)
          end-valid? (>= consensus end start)
          range-valid? (<= (- end start) config/max-range)]
      (cond
        (not start-valid?)
        (bad-request-response (error (str "Invalid start: " start ".")))

        (not end-valid?)
        (bad-request-response (error (str "Invalid end: " end ".")))

        (not range-valid?)
        (bad-request-response (error (str "Invalid range: [" start ":" end "].")))

        :else
        (successful-response {:meta
                              {:start start
                               :end end
                               :total consensus}

                              :convex-web/blocks
                              (convex/blocks peer {:start start :end end})})))
    (catch Exception ex
      (log/error ex (ex-message ex))

      server-error-response)))

(defn GET-block [context index]
  (try
    (let [peer (peer/peer (system/convex-server context))
          blocks-indexed (convex/blocks-indexed peer)]
      (if-let [block (get blocks-indexed (Long/parseLong index))]
        (successful-response block)
        (not-found-response {:error {:message (str "Block " index " doesn't exist.")}})))
    (catch Exception ex
      (log/error ex (ex-message ex))

      server-error-response)))

(defn GET-reference [_ _]
  (try
    (successful-response (convex/reference))
    (catch Exception ex
      (log/error ex (ex-message ex))

      server-error-response)))

(defn GET-markdown-page [_ request]
  (try
    (let [page (get-in request [:query-params "page"])
          contents (read-markdown-page (keyword page))]
      (cond
        (nil? contents)
        (not-found-response (error "Markdown page not found."))

        :else
        (successful-response contents)))
    (catch Exception ex
      (log/error ex (ex-message ex))

      server-error-response)))

(defn app [context]
  (routes
    (GET "/" req (index req))

    ;; -- Internal API
    (GET "/api/internal/session" req (GET-session context req))
    (POST "/api/internal/generate-account" req (POST-generate-account context req))
    (POST "/api/internal/confirm-account" req (POST-confirm-account context req))
    (POST "/api/internal/faucet" req (POST-faucet context req))
    (GET "/api/internal/accounts" req (GET-accounts context req))
    (GET "/api/internal/accounts/:address" [address] (GET-account context address))
    (GET "/api/internal/blocks" req (GET-blocks context req))
    (GET "/api/internal/blocks-range" req (GET-blocks-range context req))
    (GET "/api/internal/blocks/:index" [index] (GET-block context index))
    (GET "/api/internal/commands" req (GET-commands context req))
    (POST "/api/internal/commands" req (POST-command context req))
    (GET "/api/internal/commands/:id" [id] (GET-command-by-id context (Long/parseLong id)))
    (GET "/api/internal/reference" req (GET-reference context req))
    (GET "/api/internal/markdown-page" req (GET-markdown-page context req))

    (route/resources "/")
    (route/not-found "<h1>Page not found</h1>")))

(defn wrap-logging [handler]
  (fn wrap-logging-handler [request]
    (u/with-context
      {:logging.mdc/http-request request}
      (let [response (handler request)]
        (u/log :logging.event/endpoint :logging.mdc/http-response response)

        response))))

(defn run-server
  "Start HTTP server (default port is 8090).

   Returns `(fn [& {:keys [timeout] :or {timeout 100}}])`
   which you can call to stop the server.

   `options` are the same as org.httpkit.server/run-server."
  [system & [options]]
  (let [config {:session
                {:store (memory-session/memory-store session-ref)
                 :flash true
                 :cookie-attrs {:http-only false :same-site :strict}}}

        handler (-> (app system)
                    (wrap-logging)
                    (wrap-defaults (merge-with merge site-defaults config)))]
    (http-kit/run-server handler options)))

