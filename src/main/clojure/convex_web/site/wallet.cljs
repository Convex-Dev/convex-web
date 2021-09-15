(ns convex-web.site.wallet
  (:require
   [convex-web.site.session :as session]
   [convex-web.site.store :as store]
   [convex-web.site.gui :as gui]
   [convex-web.site.format :as format]

   [reitit.frontend.easy :as rfe]
   [convex-web.site.stack :as stack]

   ["@heroicons/react/solid" :refer [PlusIcon]]))

(defn WalletPage [_ _ _]
  (let [accounts (session/?accounts)]
    [:div.flex.flex-col.items-start.space-y-12

     [gui/PrimaryButton
      {:on-click #()}
      [:div.flex.items-center.space-x-2
       {:class gui/button-child-small-padding}
       [:> PlusIcon
        {:className "w-5 h-5 text-white"}]

       [:span.block.text-sm.uppercase.text-white
        "Add Account"]]]

     (if (seq accounts)
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
           (for [{:convex-web.account/keys [address]} accounts]
             (let [td-class ["text-xs text-gray-700 whitespace-no-wrap px-2"]]
               ^{:key address}
               [:tr.cursor-default
                
                [:td {:class td-class}
                 [:div.flex.items-center
                  [gui/AIdenticon {:value address :size gui/identicon-size-large}]
                  
                  [:a.hover:underline.ml-2
                   {:href (rfe/href :route-name/testnet.account {:address address})}
                   [:code.text-xs (format/prefix-# address)]]]]
                
                [:td {:class td-class}
                 [:code.text-xs.font-bold.text-indigo-500
                  (let [account (store/?account address)]
                    (format/format-number (get-in account [:convex-web.account/status :convex-web.account-status/balance])))]]])))]]
       [:p.prose
        "You currently have no active account. Either create a new account, or import one using a previously created wallet key."])
     
     [gui/PrimaryButton
      {:on-click #(stack/push :page.id/session {:modal? true
                                                :title "Restore Wallet Key"})}
      [:span.block.text-sm.uppercase.text-white
       {:class gui/button-child-large-padding}
       "Restore Wallet Key"]]]))

(def wallet-page
  #:page {:id :page.id/testnet.wallet
          :title "Wallet"
          :description "Your accounts on the current test network and their associated keys are managed for you in this wallet."
          :component #'WalletPage})
