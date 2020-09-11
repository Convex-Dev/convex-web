(ns convex-web.site.wallet
  (:require [convex-web.site.session :as session]
            [convex-web.site.store :as store]
            [convex-web.site.runtime :refer [sub]]
            [convex-web.site.gui :as gui]
            [convex-web.site.format :as format]

            [reitit.frontend.easy :as rfe]
            [convex-web.site.stack :as stack]))

(defn WalletPage [_ _ _]
  [:div.flex.flex-col.items-start.space-y-2
   (doall
     (for [{:convex-web.account/keys [address]} (session/?accounts)]
       ^{:key address}
       [:div.flex.items-center
        [gui/Identicon {:value address :size 40}]

        [gui/Tooltip
         {:title "Address"}
         [:a.hover:underline.ml-2
          {:href (rfe/href :route-name/account-explorer {:address address})}
          [:code.text-xs (format/address-blob address)]]]

        [gui/Tooltip
         {:title "Balance"}
         [:code.text-xs.font-bold.text-indigo-500.ml-4
          (let [account (store/?account address)]
            (format/format-number (get-in account [:convex-web.account/status :convex-web.account-status/balance])))]]]))

   [:div.my-2]

   [gui/DefaultButton
    {:on-click #(stack/push :page.id/transfer (merge {:modal? true}
                                                     (when-let [from (session/?active-address)]
                                                       {:state
                                                        {:convex-web/transfer
                                                         {:convex-web.transfer/from from}}})))}
    [:span.text-xs.uppercase "Transfer"]]])

(def wallet-page
  #:page {:id :page.id/wallet
          :title "Wallet"
          :component #'WalletPage})
