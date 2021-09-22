(ns convex-web.site.invoke
  (:require
   [ajax.core :refer [POST]]))

(defn csrf-header []
  {"x-csrf-token" (.-value (.getElementById js/document "__anti-forgery-token"))})

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

(defn wallet-account-key-pair [{:keys [body handler error-handler]}]
  (POST-invoke {:params (invoke-params :convex-web.invoke/wallet-account-key-pair body)
                :handler handler
                :error-handler error-handler}))

(defn wallet-add-account [{:keys [body handler error-handler]}]
  (POST-invoke {:params (invoke-params :convex-web.invoke/wallet-add-account body)
                :handler handler
                :error-handler error-handler}))

(defn wallet-remove-account [{:keys [body handler error-handler]}]
  (POST-invoke {:params (invoke-params :convex-web.invoke/wallet-remove-account body)
                :handler handler
                :error-handler error-handler}))