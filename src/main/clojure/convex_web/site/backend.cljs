(ns convex-web.site.backend
  (:require
   [clojure.string :as str]
   [clojure.spec.alpha :as s]

   [ajax.core :refer [GET POST]]

   [convex-web.specs]))

(defn csrf-header []
  {"x-csrf-token" (.-value (.getElementById js/document "__anti-forgery-token"))})

(defn GET-session [{:keys [handler error-handler]}]
  (GET "/api/internal/session" (merge {:handler handler}
                                      (when error-handler
                                        {:error-handler error-handler}))))

(defn GET-accounts
  "Gets a range of Accounts."
  [{:keys [start end handler error-handler]}]
  (let [params (merge (when start
                        {:start start})
                      (when end
                        {:end end}))]
    (GET "/api/internal/accounts" (merge {:handler handler}
                                         (when error-handler
                                           {:error-handler error-handler})
                                         (when params
                                           {:params params})))))

(defn GET-account [address {:keys [handler error-handler]}]
  (let [address (str/replace (str address) #"#" "")]
    (GET (str "/api/internal/accounts/" address)
      (merge {:handler handler}
        (when error-handler
          {:error-handler error-handler})))))

(defn GET-blocks
  "Gets the latest `n` Blocks, or start from `i` and take `n`."
  [{:keys [handler error-handler]}]
  (GET "/api/internal/blocks" (merge {:handler handler}
                                     (when error-handler
                                       {:error-handler error-handler}))))

(defn GET-blocks-range
  "Gets a range of Blocks."
  [{:keys [start end handler error-handler]}]
  (let [params (merge (when start
                        {:start start})
                      (when end
                        {:end end}))]
    (GET "/api/internal/blocks-range" (merge {:handler handler}
                                             (when params
                                               {:params params})
                                             (when error-handler
                                               {:error-handler error-handler})))))

(defn GET-block [index {:keys [handler error-handler]}]
  (GET (str "/api/internal/blocks/" index) (merge {:handler handler}
                                                  (when error-handler
                                                    {:error-handler error-handler}))))

(defn POST-generate-account [{:keys [handler error-handler]}]
  (POST "/api/internal/generate-account" (merge {:headers (csrf-header)
                                                 :handler handler}
                                                (when error-handler
                                                  {:error-handler error-handler}))))

(defn POST-confirm-account [address {:keys [handler error-handler]}]
  (POST "/api/internal/confirm-account" (merge {:headers (csrf-header)
                                                :handler handler
                                                :params address}
                                               (when error-handler
                                                 {:error-handler error-handler}))))

(defn POST-command [command {:keys [handler error-handler]}]
  (POST "/api/internal/commands" (merge {:headers (csrf-header)
                                         :handler handler
                                         :params command}
                                        (when error-handler
                                          {:error-handler error-handler}))))

(defn GET-command [id {:keys [handler error-handler]}]
  (GET (str "/api/internal/commands/" id) (merge {:handler handler}
                                                 (when error-handler
                                                   {:error-handler error-handler}))))

(defn POST-faucet [faucet {:keys [handler error-handler]}]
  (POST "/api/internal/faucet" (merge {:headers (csrf-header)
                                       :handler handler
                                       :params faucet}
                                      (when error-handler
                                        {:error-handler error-handler}))))

(defn GET-reference [{:keys [handler error-handler]}]
  (GET "/api/internal/reference" (merge {:handler handler}
                                        (when error-handler
                                          {:error-handler error-handler}))))

(defn GET-markdown-page [k {:keys [handler error-handler]}]
  (GET "/api/internal/markdown-page" (merge {:params {"page" k}
                                             :handler handler}
                                            (when error-handler
                                              {:error-handler error-handler}))))


(defn POST-env [{:keys [params handler error-handler]}]
  (POST "/api/internal/env" (merge {:headers (csrf-header)
                                    :handler handler
                                    :params params}
                              (when error-handler
                                {:error-handler error-handler}))))

(s/def :POST-env.params/address :convex-web/address)

(s/def :POST-env.params/sym symbol?)

(s/def :POST-env/params
  (s/keys :req-un [:POST-env.params/address
                   :POST-env.params/sym]))

(s/def :POST-env/args
  (s/keys :req-un [:POST-env/params]))

(s/fdef POST-env
  :args (s/cat :args :POST-env/args))

;; ---

;; TODO: Delete
(defn POST-add-account [{:keys [params handler error-handler]}]
  (POST "/api/internal/add-account" (merge {:headers (csrf-header)
                                            :handler handler
                                            :params params}
                                      (when error-handler
                                        {:error-handler error-handler}))))


;; ---

(defn POST-invoke [{:keys [params handler error-handler]}]
  (POST "/api/internal/invoke"
    (merge {:headers (csrf-header)
            :handler handler
            :params params}
      (when error-handler
        {:error-handler error-handler}))))

(defn invoke-params [id body]
  {:convex-web.invoke/id id
   :convex-web.invoke/body body})

(defn invoke-wallet-account-key-pair [{:keys [body handler error-handler]}]
  (POST-invoke {:params (invoke-params :convex-web.invoke/wallet-account-key-pair body)
                :handler handler
                :error-handler error-handler}))

(defn invoke-wallet-add-account [{:keys [body handler error-handler]}]
  (POST-invoke {:params (invoke-params :convex-web.invoke/wallet-add-account body)
                :handler handler
                :error-handler error-handler}))


;; ---


(defn POST-transaction-prepare [{:keys [address source] :as params} {:keys [handler error-handler]}]
  (POST "/api/v1/transaction/prepare" (merge {:handler handler
                                              :format :json
                                              :params params}
                                             (when error-handler
                                               {:error-handler error-handler}))))

(defn POST-transaction-submit [{:keys [hash] :as params} {:keys [handler error-handler]}]
  (POST "/api/v1/transaction/submit" (merge {:handler handler
                                             :format :json
                                             :params params}
                                            (when error-handler
                                              {:error-handler error-handler}))))

(defn GET-state
  "Gets the current state of the Convex network."
  [{:keys [handler error-handler]}]
  (GET "/api/internal/state" (merge {:handler handler}
                                    (when error-handler
                                      {:error-handler error-handler}))))