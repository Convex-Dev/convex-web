(ns convex-web.site.wallet
  (:require [convex-web.site.session :as session]
            [convex-web.site.store :as store]
            [convex-web.site.runtime :refer [sub]]
            [convex-web.site.gui :as gui]
            [convex-web.site.format :as format]

            [reitit.frontend.easy :as rfe]
            [convex-web.site.stack :as stack]))

(defn WalletPage [_ _ _]
  [:div.flex.flex-col.items-start.space-y-12
   [:table.text-left.table-auto
    [:thead
     (let [th-class "text-xs uppercase text-gray-600 sticky top-0"]
       [:tr.select-none
        [:th {:class th-class}
         "Address"]
        
        [:th
         {:class th-class}
         "Balance"]])]
    
    [:tbody
     (doall
       (for [{:convex-web.account/keys [address]} (session/?accounts)]
         (let [td-class ["text-xs text-gray-700 whitespace-no-wrap px-2"]]
           ^{:key address}
           [:tr.cursor-default
            
            [:td {:class td-class}
             [:div.flex.items-center
              [gui/AIdenticon {:value address :size gui/identicon-size-large}]
              
              [:a.hover:underline.ml-2
               {:href (rfe/href :route-name/account-explorer {:address address})}
               [:code.text-xs (format/prefix-# address)]]]]
            
            [:td {:class td-class}
             [:code.text-xs.font-bold.text-indigo-500
              (let [account (store/?account address)]
                (format/format-number (get-in account [:convex-web.account/status :convex-web.account-status/balance])))]]])))]]
   
   [gui/PrimaryButton
    {:on-click #(stack/push :page.id/session {:modal? true
                                              :title "Restore Wallet Key"})}
    [:span.block.text-sm.uppercase.text-white
     {:class gui/button-child-large-padding}
     "Restore Wallet Key"]]])

(def wallet-page
  #:page {:id :page.id/wallet
          :title "Wallet"
          :description "This is your Convex Wallet, managed for your convenience on the test network at convex.world."
          :component #'WalletPage})
