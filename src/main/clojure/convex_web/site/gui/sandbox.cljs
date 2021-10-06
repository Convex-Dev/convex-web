(ns convex-web.site.gui.sandbox
  (:require
   [goog.string.format]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]

   [convex-web.site.format :as format]
   [convex-web.site.backend :as backend]
   [convex-web.site.gui :as gui]
   [convex-web.site.gui.account :as guia]
   [convex-web.site.sandbox.renderer :as renderer]
   [convex-web.convex :as convex]))

(defn AddressRenderer [address]
  (r/with-let [account-ref (r/atom {:ajax/status :ajax.status/pending})

               _ (backend/GET-account
                   address
                   {:handler
                    (fn [account]
                      (reset! account-ref {:account account
                                           :ajax/status :ajax.status/success}))

                    :error-handler
                    (fn [error]
                      (reset! account-ref {:ajax/status :ajax.status/error
                                           :ajax/error error}))})]

    (let [{account :account
           ajax-status :ajax/status} @account-ref

          address (convex/address address)]

      [:div.w-full.max-w-prose.bg-white.rounded.shadow.p-3
       (case ajax-status
         :ajax.status/pending
         [gui/SpinnerSmall]

         :ajax.status/error
         [:div.flex.flex-col.space-y-1
          [:div.flex.items-center.space-x-1
           [gui/AIdenticon {:value (str address) :size gui/identicon-size-small}]

           [:span.font-mono.text-xs.truncate
            (format/prefix-# address)]]

          [:span.text-xs (get-in @account-ref [:ajax/error :response :error :message])]]

         :ajax.status/success
         [:div.flex.flex-col.space-y-3

          ;; Address & Refresh.
          [:div.flex.justify-between
           [:a.inline-flex.items-center.space-x-1
            {:href (rfe/href :route-name/testnet.account {:address address})}
            [gui/AIdenticon {:value (str address) :size gui/identicon-size-small}]

            [:span.font-mono.text-xs.truncate
             {:class gui/hyperlink-hover-class}
             (format/prefix-# address)]]

           [gui/Tooltip
            {:title "Refresh"
             :size "small"}
            [gui/DefaultButton
             {:on-click
              (fn []
                ;; Store the status of this request in a place specific to refresh
                ;; because we don't want the whole UI to transition to pending.
                (swap! account-ref assoc-in [:refresh :ajax/status] :ajax.status/pending)

                ;; Reset address's env persisted in session.
                (rf/dispatch [:session/!set-state #(assoc-in % [address :env] {})])

                (backend/GET-account
                  address
                  {:handler
                   (fn [account]
                     (swap! account-ref merge {:account account
                                               :refresh {:ajax/status :ajax.status/success}}))

                   :error-handler
                   (fn [_]
                     (swap! account-ref merge {:refresh {:ajax/status :ajax.status/error}}))}))}
             (if (= :ajax.status/pending (get-in @account-ref [:refresh :ajax/status]))
               [gui/SpinnerSmall]
               [gui/RefreshIcon {:class "w-4 h-4"}])]]]

          ;; Balance & Type.
          [:div.flex.space-x-8
           ;; -- Balance.
           (let [balance (get-in @account-ref [:account :convex-web.account/status :convex-web.account-status/balance])]
             [:div.flex.flex-col
              [:span.text-xs.text-indigo-500.uppercase "Balance"]
              [:div.flex.flex-col.flex-1.justify-center
               [:span.text-xs (format/format-number balance)]]])

           ;; -- Type.
           (let [type (get-in @account-ref [:account :convex-web.account/status :convex-web.account-status/type])]
             [:div.flex.flex-col
              [:span.text-xs.text-indigo-500.uppercase "Type"]
              [:div.flex.flex-col.flex-1.justify-center
               [:span.text-xs.uppercase type]]])]

          [guia/EnvironmentBrowser
           {:convex-web/account account}]])])))

(defn BlobRenderer [object]
  [:div.flex.flex-1.bg-white.rounded.shadow
   [:div.flex.flex-col.p-2
    [:span.text-xs.text-indigo-500.uppercase.mt-2
     "Blob"]
    [:div.flex
     [:code.text-xs.mr-2
      object]

     [gui/ClipboardCopy object]]]])

(defn ResultRenderer [result]
  (let [{result-type :convex-web.result/type
         result-value :convex-web.result/value
         result-interactive? :convex-web.result/interactive?
         result-interactive :convex-web.result/interactive} result]
    (cond
      result-interactive?
      (renderer/compile result-interactive)

      (= result-type "Address")
      [AddressRenderer result-value]

      (= result-type "Blob")
      [BlobRenderer result-value]

      (= result-type "Function")
      (let [{result-value :convex-web.result/value
             result-metadata :convex-web.result/metadata} result]
        (if result-metadata
          [:div.flex.flex-1.bg-white.rounded.shadow
           [gui/SymbolMeta
            {:symbol result-value
             :metadata result-metadata
             :show-examples? false}]]
          [gui/Highlight result-value]))

      :else
      [:code.text-xs
       result-value])))