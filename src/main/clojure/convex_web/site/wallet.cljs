(ns convex-web.site.wallet
  (:require
   [clojure.string :as str]

   [reitit.frontend.easy :as rfe]
   [re-frame.core :as rf]

   [convex-web.site.stack :as stack]
   [convex-web.site.session :as session]
   [convex-web.site.store :as store]
   [convex-web.site.gui :as gui]
   [convex-web.site.format :as format]
   [convex-web.site.backend :as backend]

   ["@heroicons/react/solid" :as icon]))

(def input-style
  ["w-full h-10"
   "px-4"
   "rounded-md"
   "bg-blue-100"
   "font-mono text-xs"
   "focus:outline-none focus:ring focus:border-blue-300"])

(defn AccountKeyPairPage [_ state _]
  (let [{:keys [address
                convex-web.key-pair/account-key
                convex-web.key-pair/private-key
                ajax/status]} state]

    [:div.flex.flex-col.space-y-8.p-6
     {:class "w-[60vw]"}

     ;; -- Address
     [:div.flex.items-center.space-x-1
      [gui/AIdenticon {:value address :size gui/identicon-size-large}]

      [:a.hover:underline
       {:href (rfe/href :route-name/testnet.account {:address address})}
       [:code.text-base (format/prefix-# address)]]

      (when (= status :ajax.status/pending)
        [gui/SpinnerSmall])]

     [:div.flex.flex-col.space-y-6

      ;; -- Public Key
      [:div.flex.flex-col.space-y-1

       [gui/Caption
        "Account Key"]

       [:input
        {:class input-style
         :type "text"
         :value (or account-key "")
         :readOnly true}]]

      ;; -- Private Key
      [:div.flex.flex-col.space-y-1

       [gui/Caption
        "Private Key"]

       [:input
        {:class input-style
         :type "text"
         :value (or private-key "")
         :readOnly true}]]]]))

(def account-key-pair-page
  #:page {:id :page.id/account-key-pair
          :title "Key Pair"
          :component #'AccountKeyPairPage
          :on-push
          (fn [_ state set-state]
            (let [{:keys [address]} state]
              (set-state merge {:ajax/status :ajax.status/pending})

              (backend/POST-wallet-account-key-pair
                {:params {:address address}

                 :handler
                 (fn [key-pair]
                   (set-state merge key-pair {:ajax/status :ajax.status/success}))

                 :error-handler
                 (fn [error]
                   (set-state merge {:ajax/status :ajax.status/error
                                     :error error}))})))})


(defn AddAccountPage [_ state set-state]
  (let [{:keys [address account-key private-key ajax/status]} state

        pending? (= status :ajax.status/pending)]
    [:div.flex.flex-col.space-y-8.p-6
     {:class "w-[50vw]"}

     [:div.flex.flex-col.space-y-6

      ;; -- Address
      [:div.flex.flex-col.space-y-2

       [gui/Caption
        "Address"]

       [:input
        {:class input-style
         :type "text"
         :value address
         :on-change
         #(set-state assoc :address (gui/event-target-value %))}]]

      ;; -- Public Key
      [:div.flex.flex-col.space-y-1

       [gui/Caption
        "Account Key"]

       [:input
        {:class input-style
         :type "text"
         :value account-key
         :on-change
         #(set-state assoc :account-key (gui/event-target-value %))}]]

      ;; -- Private Key
      [:div.flex.flex-col.space-y-1

       [gui/Caption
        "Private Key"]

       [:input
        {:class input-style
         :type "text"
         :value private-key
         :on-change
         #(set-state assoc :private-key (gui/event-target-value %))}]]]

     ;; -- Confirm
     [gui/PrimaryButton
      {:disabled (or pending? (str/blank? address))
       :on-click
       (fn []
         (set-state assoc :ajax/status :ajax.status/pending)

         (backend/POST-add-account
           {:params {:address address
                     :account-key account-key
                     :private-key private-key}

            :handler (fn [session]
                       (set-state assoc :ajax/status :ajax.status/success)

                       (session/refresh session)

                       (stack/pop))

            :error-handler (fn [_]
                             (set-state assoc :ajax/status :ajax.status/error))}))}

      [:div.relative
       [:span.block.text-sm.uppercase.text-white
        {:class gui/button-child-small-padding}
        "Confirm"]

       (when pending?
         [:div.absolute.inset-0.flex.justify-center.items-center
          [gui/SpinnerSmall]])]]]))

(def add-account-page
  #:page {:id :page.id/add-account
          :title "Add Account to Wallet"
          :description "Add an existing account to your wallet."
          :component #'AddAccountPage})


(defn WalletPage [_ state set-state]
  (let [accounts (session/?accounts)

        {:keys [show-wallet-key?]} state]
    [:div.flex.flex-col.items-start.space-y-12

     ;; -- Wallet Key

     [:div.flex.flex-col
      [:div.flex.items-center.space-x-2
       [:span.text-base.text-gray-500
        "Wallet Key"]

       [gui/Tooltip
        {:title (if show-wallet-key?
                  "Hide Wallet Key"
                  "Show Wallet Key")
         :size "small"}
        [:button.p-2.rounded.hover:shadow.hover:bg-gray-100.active:bg-gray-200
         {:on-click #(set-state update :show-wallet-key? not)}

         (if show-wallet-key?
           [:> icon/EyeOffIcon
            {:className "w-4 h-4 text-gray-500"}]
           [:> icon/EyeIcon
            {:className "w-4 h-4 text-gray-500"}])]]]

      (if show-wallet-key?
        [:code.text-sm.text-gray-500 "********"]
        [:div.flex.items-center
         [:code.text-sm.mr-2 @(rf/subscribe [:session/?id])]
         [gui/ClipboardCopy @(rf/subscribe [:session/?id])]])]


     [:div.flex
      {:class
       (if (seq accounts)
         "flex-row space-x-16"
         "flex-col space-y-8")}

      ;; -- Accounts

      (if (seq accounts)
        [:table.text-left.table-auto
         [:thead
          (let [th-class "text-xs uppercase text-gray-600 sticky top-0"]
            [:tr.select-none
             [:th {:class th-class}
              "Address"]

             [:th
              {:class th-class}
              "Balance"]

             [:th
              {:class th-class}
              "Key Pair"]])]

         [:tbody
          (doall
            (for [{:convex-web.account/keys [address]} accounts]
              (let [td-class ["text-xs text-gray-700 whitespace-no-wrap px-2"]]
                ^{:key address}
                [:tr.cursor-default

                 ;; -- Address
                 [:td {:class td-class}
                  [:div.flex.items-center
                   [gui/AIdenticon {:value address :size gui/identicon-size-large}]

                   [:a.hover:underline.ml-2
                    {:href (rfe/href :route-name/testnet.account {:address address})}
                    [:code.text-xs (format/prefix-# address)]]]]

                 ;; -- Balance
                 [:td {:class td-class}
                  [:code.text-xs.font-bold.text-indigo-500
                   (let [account (store/?account address)]
                     (format/format-number (get-in account [:convex-web.account/status :convex-web.account-status/balance])))]]

                 ;; - Key Pair
                 [:td {:class td-class}
                  [gui/Tooltip
                   {:title "View Key Pair"
                    :size "small"}
                   [:button.p-2.rounded.hover:shadow.hover:bg-gray-100.active:bg-gray-200
                    {:on-click #(stack/push :page.id/account-key-pair {:modal? true
                                                                       :state {:address address}})}
                    [:> icon/KeyIcon
                     {:className "w-5 h-5"}]]]]])))]]
        [:p.prose
         "You currently have no active account. Either create a new account, or import one using a previously created wallet key."])


      ;; -- Add & Restore

      [:div.flex.flex-col.items-start.space-y-4
       [gui/Tooltip
        {:title "Add an existing account to your Wallet"
         :size "small"}

        [gui/PrimaryButton
         {:on-click #(stack/push :page.id/add-account {:modal? true})}
         [:div.flex.items-center.space-x-2
          {:class gui/button-child-small-padding}
          [:> icon/PlusIcon
           {:className "w-5 h-5 text-white"}]

          [:span.block.text-sm.uppercase.text-white
           "Add Account"]]]]

       [gui/PrimaryButton
        {:on-click #(stack/push :page.id/session {:modal? true
                                                  :title "Restore Wallet Key"})}
        [:span.block.text-sm.uppercase.text-white
         {:class gui/button-child-small-padding}
         "Restore Wallet Key"]]]]]))

(def wallet-page
  #:page {:id :page.id/testnet.wallet
          :title "Wallet"
          :description "Your accounts on the current test network and their associated keys are managed for you in this wallet."
          :component #'WalletPage})
