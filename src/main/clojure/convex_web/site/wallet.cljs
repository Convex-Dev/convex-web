(ns convex-web.site.wallet
  (:require
   [clojure.string :as str]

   [lambdaisland.glogi :as log]

   [reitit.frontend.easy :as rfe]
   [re-frame.core :as rf]

   [convex-web.site.stack :as stack]
   [convex-web.site.session :as session]
   [convex-web.site.store :as store]
   [convex-web.site.gui :as gui]
   [convex-web.site.format :as format]
   [convex-web.site.invoke :as invoke]

   ["@heroicons/react/solid" :as icon]))

(def input-style
  ["w-full h-10"
   "px-4"
   "rounded-md"
   "bg-blue-100"
   "font-mono text-xs"
   "focus:outline-none focus:ring focus:border-blue-300"])

(defn AccountKeyPairPage [_ state _]
  (let [{:convex-web.account/keys [address key-pair]} state

        {:convex-web.key-pair/keys [seed account-key private-key]} key-pair

        seed (some-> seed format/prefix-0x)
        account-key (some-> account-key format/prefix-0x)
        private-key (some-> private-key format/prefix-0x)]

    [:div.flex.flex-col.space-y-8.p-6
     {:class "w-[60vw]"}

     ;; -- Address
     [:div.flex.items-center.space-x-1
      [gui/AIdenticon {:value address :size gui/identicon-size-large}]

      [:a.hover:underline
       {:href (rfe/href :route-name/testnet.account {:address address})}
       [:code.text-base (format/prefix-# address)]]]

     [:div.flex.flex-col.space-y-6

      ;; -- Seed
      [:div.flex.flex-col.space-y-1

       [gui/Caption
        "Seed"]

       [:input
        {:class input-style
         :type "text"
         :value (or seed "")
         :readOnly true}]]

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
          :state-spec :convex-web/signer})


(defn AddAccountPage [_ state set-state]
  (let [{:keys [using-choice address seed account-key private-key error ajax/status]} state

        pending? (= status :ajax.status/pending)

        using-choice (or using-choice :keys)]
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


      ;; -- Using Keys / Seed
      [:div.flex.space-x-2

       ;; -- Using keys
       [:label
        [:input
         {:type "radio"
          :value "keys"
          :checked (= using-choice :keys)
          :on-change #(set-state assoc :using-choice :keys)}]

        [:span.text-xs.text-gray-700.uppercase.ml-1 "Using Keys"]]

       ;; -- Using Seed
       [:label
        [:input
         {:type "radio"
          :value "seed"
          :checked (= using-choice :seed)
          :on-change #(set-state assoc :using-choice :seed)}]

        [:span.text-xs.text-gray-700.uppercase.ml-1 "Using Seed"]]]


      ;; -- Using

      (if (= using-choice :seed)
        ;; Using seed.
        [:div.flex.flex-col.space-y-1

         [gui/Caption
          "Seed"]

         [:input
          {:class input-style
           :type "text"
           :value seed
           :on-change
           #(set-state assoc :seed (gui/event-target-value %))}]]

        ;; Else; using keys.
        [:<>
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
            #(set-state assoc :private-key (gui/event-target-value %))}]]])]

     (when (= :ajax.status/error status)
       [:span.text-sm.text-red-500
        (or (get-in error [:response :error :message])
          (str (:status error) " " (:status-text error)))])

     ;; -- Confirm
     [gui/PrimaryButton
      {:disabled (or pending? (str/blank? address))
       :on-click
       (fn []
         (set-state assoc :ajax/status :ajax.status/pending)

         (invoke/wallet-add-account
           {:body (merge {:address address}

                    (when seed
                      {:seed seed})

                    (when account-key
                      {:account-key account-key})

                    (when private-key
                      {:private-key private-key}))

            :handler (fn [session]
                       (set-state assoc :ajax/status :ajax.status/success)

                       (session/refresh session)

                       (stack/pop))

            :error-handler (fn [error]
                             (set-state merge {:ajax/status :ajax.status/error
                                               :error error}))}))}

      [:div.relative
       [:span.block.text-sm.uppercase.text-white
        {:class gui/button-child-large-padding}
        "Confirm"]

       (when pending?
         [:div.absolute.inset-0.flex.justify-center.items-center
          [gui/SpinnerSmall]])]]]))

(def add-account-page
  #:page {:id :page.id/add-account
          :title "Add existing Account to Wallet"
          :description "Add an existing account to your wallet."
          :component #'AddAccountPage})

(defn RemoveAccountPage [_ state set-state]
  (let [{:keys [address account-key private-key error ajax/status]} state

        pending? (= status :ajax.status/pending)]
    [:div.flex.flex-col.space-y-8.p-6
     {:class "w-[50vw]"}

     [:p.prose.prose-base
      "You can always add this account to your wallet later, but if it's an account you own, please make sure you have a copy of the private key."]

     [:div.flex.flex-col.space-y-6

      ;; -- Address
      [:div.flex.flex-col.space-y-2

       [gui/Caption
        "Address"]

       [:input
        {:class input-style
         :type "text"
         :value address
         :readOnly true
         :on-change
         #(set-state assoc :address (gui/event-target-value %))}]]]

     (when (= :ajax.status/error status)
       [:span.text-sm.text-red-500
        (or (get-in error [:response :error :message])
          (str (:status error) " " (:status-text error)))])

     ;; -- Confirm
     [gui/RedButton
      {:disabled (or pending? (str/blank? address))
       :on-click
       (fn []
         (set-state assoc :ajax/status :ajax.status/pending)

         (invoke/wallet-remove-account
           {:body (merge {:address address}

                    (when account-key
                      {:account-key account-key})

                    (when private-key
                      {:private-key private-key}))

            :handler (fn [session]
                       (set-state assoc :ajax/status :ajax.status/success)

                       ;; Update session's accounts and selected address, but preserve its state.
                       (rf/dispatch [:session/!update (fn [current-session]
                                                        (let [session (merge current-session session)

                                                              {session-accounts :convex-web.session/accounts
                                                               selected-address :convex-web.session/selected-address} session

                                                              session-addresses (into #{} (map :convex-web.account/address session-accounts))

                                                              selected-address-exists? (contains? session-addresses selected-address)]

                                                          ;; Remove active address if it no longer exists in the wallet.
                                                          (cond-> session
                                                            (false? selected-address-exists?)
                                                            (dissoc :convex-web.session/selected-address))))])

                       (stack/pop))

            :error-handler (fn [error]
                             (set-state merge {:ajax/status :ajax.status/error
                                               :error error}))}))}

      [:div.relative
       [:span.block.text-sm.uppercase.text-white
        {:class gui/button-child-large-padding}
        "Confirm"]

       (when pending?
         [:div.absolute.inset-0.flex.justify-center.items-center
          [gui/SpinnerSmall]])]]]))

(def remove-account-page
  #:page {:id :page.id/wallet-remove-account
          :title "Remove Account from Wallet"
          :description "Remove account from your wallet."
          :component #'RemoveAccountPage})


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
           [:> icon/EyeIcon
            {:className "w-4 h-4 text-gray-500"}]
           [:> icon/EyeOffIcon
            {:className "w-4 h-4 text-gray-500"}])]]]

      (if show-wallet-key?
        (let [sid @(rf/subscribe [:session/?id])]
          [:div.flex.items-center
           [:code.text-sm.mr-2 sid]
           [gui/ClipboardCopy sid]])
        [:code.text-sm.text-gray-500 "········"])]


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
              "Key Pair"]

             [:th
              {:class th-class}
              ""]])]

         [:tbody
          (doall
            (for [{:convex-web.account/keys [address key-pair] :as account} accounts]
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

                 ;; -- Key Pair
                 [:td {:class td-class}
                  [gui/Tooltip
                   {:title (if key-pair
                             "View Key Pair"
                             "Account doesn't have a key pair")
                    :size "small"}
                   [:button.p-2.rounded.hover:shadow.hover:bg-gray-100.active:bg-gray-200
                    {:class (if key-pair
                              "text-gray-800"
                              "text-gray-400 pointer-events-none")
                     :on-click #(stack/push :page.id/account-key-pair
                                  {:modal? true
                                   :state account})}
                    [:> icon/KeyIcon
                     {:className "w-5 h-5"}]]]]

                 ;; -- Remove
                 [:td {:class td-class}
                  [gui/Tooltip
                   {:title "Remove account"
                    :size "small"}
                   [:button.p-2.rounded.hover:shadow.hover:bg-gray-100.active:bg-gray-200
                    {:on-click #(stack/push :page.id/wallet-remove-account
                                  {:modal? true
                                   :state {:address address}})}
                    [:> icon/TrashIcon
                     {:className "w-5 h-5"}]]]]])))]]
        [:p.prose
         "You currently have no active account. Either create a new account, or import one using a previously created wallet key."])


      ;; -- Add & Restore

      [:div.flex.flex-col.space-y-4
       {:class
        (if (seq accounts)
          "items-strech"
          "items-start")}

       ;; -- Add Account

       [gui/Tooltip
        {:title "Add an existing account to your wallet"
         :size "small"}

        [:button
         {:class
          ["w-[300px]"
           "rounded"
           "shadow-md"
           "bg-blue-500 hover:bg-blue-400 active:bg-blue-600"]
          :on-click #(stack/push :page.id/add-account {:modal? true})}
         [:div.flex.items-center.space-x-2
          {:class gui/button-child-large-padding}
          [:> icon/PlusIcon
           {:className "w-5 h-5 text-white"}]

          [:span.block.text-sm.uppercase.text-white
           "Add existing account"]]]]


       ;; -- Restore Wallet

       [gui/Tooltip
        {:title "Restore an existing wallet"
         :size "small"}

        [:button
         {:class
          ["w-[300px]"
           "rounded"
           "shadow-md"
           "bg-blue-500 hover:bg-blue-400 active:bg-blue-600"]
          :on-click #(stack/push :page.id/session {:modal? true
                                                   :title "Restore Wallet"})}
         [:span.block.text-sm.uppercase.text-white
          {:class gui/button-child-large-padding}
          "Restore Wallet"]]]]]]))

(def wallet-page
  #:page {:id :page.id/testnet.wallet
          :title "Wallet"
          :description "Your accounts on the current test network and their associated keys are managed for you in this wallet."
          :component #'WalletPage})
