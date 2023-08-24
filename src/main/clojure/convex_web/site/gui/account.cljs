(ns convex-web.site.gui.account
  (:require
   [clojure.string :as str]

   [reagent.core :as r]
   [re-frame.core :as rf]
   [cljfmt.core :as cljfmt]

   [convex-web.site.gui :as gui]
   [convex-web.site.stack :as stack]
   [convex-web.site.format :as format]

   ["qrcode.react" :as qrcode]))

(defn account-type-text-color [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "text-purple-500"

    (get account-status :convex-web.account-status/actor?)
    "text-indigo-500"

    :else
    "text-green-500"))

(defn account-type-bg-color [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "bg-purple-500"

    (get account-status :convex-web.account-status/actor?)
    "bg-indigo-500"

    :else
    "bg-green-500"))

(defn account-type-label [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "library"

    (get account-status :convex-web.account-status/actor?)
    "actor"

    :else
    "user"))

(defn account-type-description [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "An immutable Account containing code and other static information. A
     Library is essentially an Actor with no exported functionality."

    (get account-status :convex-web.account-status/actor?)
    "An Autonomous Actor on the Convex network, which can be used to implement
     smart contracts."

    :else
    "An external user of Convex."))

(defn merge-account-env
  "Merge account's env with session (lazy) env."
  [{:keys [convex-web/account convex-web.session/state]}]
  (let [{:convex-web.account/keys [address]} account

        ;; Construct env for success only.
        env (reduce-kv
              (fn [acc sym {:keys [value ajax/status]}]
                (when (= status :ajax.status/success)
                  (assoc acc sym value)))
              {}
              (get-in state [address :env]))]

    (update-in account [:convex-web.account/status :convex-web.account-status/environment] merge env)))

(defn EnvironmentBrowser
  "A disclousure interface to browse an account's environment.

  Lazily load the environment on click.

  Depends on session subs and events."
  [{:keys [convex-web/account]}]
  (let [state @(rf/subscribe [:session/?state])

        {:convex-web.account/keys [address]} account

        ;; Merge account env with session (lazy) env.
        account (merge-account-env {:convex-web/account account
                                    :convex-web.session/state state})

        environment (get-in account [:convex-web.account/status :convex-web.account-status/environment])]
    [:div
     [gui/Disclosure
      {:DisclosureButton (gui/disclosure-button {:text "Environment"
                                                 :color "blue"})}
      (if (seq environment)
        (into [:ul.space-y-1.mt-1]
          (map
            (fn [[sym value]]
              (let [{:keys [convex-web/lazy?]} (meta value)]
                [:li [gui/Disclosure
                      {:DisclosureButton
                       (gui/disclosure-button
                         {:text sym
                          :color "gray"
                          :on-click
                          (fn [open?]
                            ;; On open a lazy value, dispatches a query if not realized.
                            (when (and open? lazy?)
                              (when-not (get-in state [address :env sym :ajax/status])
                                (rf/dispatch [:session/!env {:address address
                                                             :sym (symbol sym)}]))))})}

                      ;; Show a spinner whilst a lazy value is realized.
                      (if lazy?
                        [:div.py-2
                         [gui/SpinnerSmall]]

                        [:code.text-xs
                         (prn-str value)])]]))
            (sort-by first environment)))

        [:p.mt-1.text-xs "Empty"])]]))

(defn CallableFunctions [account]
  (r/with-let [exports (->>
                         (get-in account [:convex-web.account/status :convex-web.account-status/exports])
                         (sort-by (comp name :name)))

               default-tab (first exports)

               state-ref (r/atom {:selected-tab default-tab})]

    (let [;; Address which will be used to execute transactions.
          active-address @(rf/subscribe [:session/?active-address])

          {;; Result of the Command.
           result :result

           ;; Ajax status of the Comand.
           ajax-status :ajax/status

           ;; Raw args as string.
           args :args

           ;; Selected tab stores the selected callable.
           selected-tab :selected-tab} @state-ref

          args-str (->>
                     (:arglists selected-tab)
                     (map
                       (fn [arg]
                         (get args arg)))
                     (str/join " "))]

      [:div.flex.space-x-10

       [:div.flex.flex-col.max-w-md.w-full.overflow-auto

        ;; Tabs.
        [:div.flex.overflow-auto
         {:class "space-x-0.5"}
         (doall
           (for [{s :name :as exported} exports]
             ^{:key s}
             [:button.rounded-none.rounded-t-lg.px-3.py-2.text-xs.border-l.border-r.border-t.focus:outline-none
              {:style
               {:min-width "90px"}

               :class
               (if (= exported selected-tab)
                 "bg-blue-50 border-blue-200 text-gray-900"
                 "bg-gray-100 hover:bg-gray-200 text-gray-500")

               :on-click
               (fn []
                 (swap! state-ref assoc
                   :args {}
                   :selected-tab exported))}

              [:span s]]))]

        ;; Args & call.
        [:div.px-3.py-5.bg-gray-50.border.rounded-b-lg

         (let [callable-syntax (some
                                 (fn [[sym syn]]
                                   (when (= sym selected-tab)
                                     syn))
                                 (get-in account [:convex-web.account/status :convex-web.account-status/environment]))

               invoke-symbol-ifn? (or
                                    (= :function (get-in callable-syntax [:convex-web.syntax/meta :doc :type]))
                                    ;; If there isn't a type, we assume it's a function.
                                    (= nil (get-in callable-syntax [:convex-web.syntax/meta :doc :type])))

               ;; Use `(call ... )` instead of `(#1/f)` if account is an actor.
               call? (get-in account [:convex-web.account/status :convex-web.account-status/actor?])

               callable-name (:name selected-tab)

               callable-address (str (:convex-web.account/address account))
               callable-address (if (str/starts-with? callable-address "#")
                                  callable-address
                                  (str "#" callable-address))

               qualified-symbol (str callable-address "/" callable-name)

               callable-source (cond
                                 call?
                                 (str "(call " callable-address " (" callable-name " " args-str "))")

                                 invoke-symbol-ifn?
                                 (str "(" qualified-symbol " " args-str ")")

                                 :else
                                 qualified-symbol)]

           [:div.flex.flex-col.items-start.space-y-3

            ;; Args.
            (map
              (fn [sym]
                ^{:key sym}
                [:div.flex.flex-col
                 [:span.text-gray-600.text-xs.font-mono
                  sym]

                 [:input.font-mono.text-xs.border.rounded.p-2.focus:outline-none.focus:border-blue-300
                  {:type "text"
                   :value (get-in @state-ref [:args sym])
                   :on-change
                   (fn [event]
                     (swap! state-ref assoc-in [:args sym] (gui/event-target-value event)))}]])
              (get-in @state-ref [:selected-tab :arglists]))

            [gui/DefaultButton
             {:on-click
              (fn []
                (swap! state-ref assoc :ajax/status :ajax.status/pending)

                (rf/dispatch
                  [:command/!execute
                   {:convex-web.command/id (random-uuid)
                    :convex-web.command/timestamp (.getTime (js/Date.))
                    :convex-web.command/mode :convex-web.command.mode/transaction
                    :convex-web.command/signer {:convex-web.account/address active-address}
                    :convex-web.command/transaction
                    {:convex-web.transaction/type :convex-web.transaction.type/invoke
                     :convex-web.transaction/source callable-source
                     :convex-web.transaction/language :convex-lisp}}
                   (fn [old-state new-state]
                     (let [command (merge old-state new-state)]

                       (swap! state-ref assoc
                         :result command
                         :ajax/status :ajax.status/success)))]))}
             "Call"]])]]

       ;; Input & output.
       (when ajax-status
         [:div.flex.flex-col.flex-1.space-y-2.max-w-md.w-full.border-2.border-blue-200.rounded-lg.p-3

          ;; Input.
          [:div.flex.flex-col.flex-1.space-y-2

           [:span.font-mono.text-gray-600.text-xs.leading-none.cursor-default
            "Input"]

           [:div.flex.flex-col.flex-1
            (cond
              (= :ajax.status/pending ajax-status)
              [gui/SpinnerSmall]

              (= :ajax.status/success ajax-status)
              (let [source (get-in result [:convex-web.command/transaction :convex-web.transaction/source])]
                [gui/Highlight
                 (try
                   (cljfmt/reformat-string source)
                   (catch js/Error _
                     source))]))]]

          ;; Output.
          [:div.flex.flex-col.flex-1.space-y-2

           [:span.font-mono.text-gray-600.text-xs.leading-none.cursor-default
            "Output"]

           [:div.flex.flex-col.flex-1.max-h-80.overflow-auto
            (cond
              (= :ajax.status/pending ajax-status)
              [gui/SpinnerSmall]

              (= :ajax.status/success ajax-status)
              [:div
               [gui/Highlight
                (get-in result [:convex-web.command/result :convex-web.result/value])]])]]])])))

(defn Account [account]
  (let [{:convex-web.account/keys [address status registry]} account

        {account-name :name
         account-description :description} registry

        {:convex-web.account-status/keys [memory-size
                                          allowance
                                          balance
                                          sequence
                                          type
                                          exports
                                          account-key
                                          controller]} status

        address-string (format/prefix-# address)

        caption-style "text-gray-600 text-base leading-none cursor-default"
        caption-container-style "flex flex-col space-y-1"
        value-style "text-sm cursor-default"

        Caption (fn [{:keys [label tooltip]}]
                  [:div.flex.space-x-1
                   [:span {:class caption-style} label]
                   [gui/InfoTooltip tooltip]])]

    [:div.flex.flex-col.items-start.space-y-8

     ;; Metadata
     ;; ==============
     (when registry
       [:div.flex.flex-col.space-y-2

        (when account-name
          [:h2.text-2xl
           account-name])

        (when account-description
          (let [style "text-sm text-gray-600"]
            (if (string? account-description)
              [:p
               {:class style}
               account-description]
              (into [:<>]
                (for [s account-description]
                  [:p
                   {:class style}
                   s])))))])


     ;; Address
     ;; ==============
     [:div.flex.flex-col
      [:div.flex.items-center.space-x-4

       ;; -- Identicon
       [gui/AIdenticon {:value address :size 88}]

       ;; -- Address
       [:div {:class caption-container-style}
        [:span {:class caption-style} "Address"]
        [:span.inline-flex.items-center
         [:span.font-mono.text-base.mr-2 address-string]]]

       ;; -- QR Code
       [:> qrcode/QRCodeSVG
        {:value (str address)
         :size 88}]]

      ;; -- Type
      [:span.inline-flex.justify-center.items-center.font-mono.text-xs.text-white.uppercase.mt-2.rounded
       {:style {:width "88px" :height "32px"}
        :class (account-type-bg-color status)}
       type]]

     ;; -- Add/Remove to/from wallet
     (let [session-accounts @(rf/subscribe [:session/?accounts])

           addresses (into #{} (map :convex-web.account/address session-accounts))]

       (if (contains? addresses address)
         [:div.flex.flex-col.space-y-2
          [gui/RedButton
           {:on-click #(stack/push :page.id/wallet-remove-account
                         {:modal? true
                          :state {:address address
                                  :account-key account-key}})}
           [:div
            {:class gui/button-child-small-padding}
            [:span.block.text-xs.uppercase.text-white
             "Remove from wallet"]]]

          [:span.text-xs.text-gray-500
           "Remove this account from your wallet."]]

         [:div.flex.flex-col.space-y-2
          [gui/PrimaryButton
           {:on-click #(stack/push :page.id/add-account
                         {:modal? true
                          :title "Add to Wallet"
                          :state {:address address
                                  :account-key account-key}})}
           [:div
            {:class gui/button-child-small-padding}
            [:span.block.text-xs.uppercase.text-white
             "Add to wallet"]]]

          [:span.text-xs.text-gray-500
           "Add this account to your wallet."]]))


     ;; Public key
     ;; ==============
     [:div.flex.items-center.space-x-8
      [:div
       [Caption
        {:label "Public Key"
         :tooltip "Public Keys may be safely shared with others, as they do not allow digital signatures to be created without the corresponding private key."}]
       [:code.text-sm (or (format/prefix-0x account-key) "-")]]]


     ;; Balance
     ;; ==============
     [:div {:class caption-container-style}
      [Caption
       {:label "Balance"
        :tooltip "Account Balance denominated in Convex Copper Coins (the smallest coin unit)"}]
      [:code.text-2xl.cursor-default (format/format-number balance)]]


     [:div.flex.w-full.md:space-x-16.space-x-6

      [:div {:class caption-container-style}
       [Caption
        {:label "Controller"
         :tooltip
         "Controller for this Account."}]
       [:code {:class value-style} (or (some->> controller (str "#")) "-")]]


      ;; -- Memory Allowance
      [:div {:class caption-container-style}
       [Caption
        {:label "Memory Allowance"
         :tooltip
         "Reserved Memory Allowance in bytes. If you create on-chain data
         beyond this amount, you will be charged extra transaction fees to
         aquire memory at the current memory pool price."}]
       [:code {:class value-style} allowance]]

      ;; -- Memory Size
      [:div {:class caption-container-style}
       [Caption
        {:label "Memory Size"
         :tooltip
         "Size in bytes of this Account, which includes any definitions you
         have created in your Enviornment."}]
       [:code {:class value-style} memory-size]]

      ;; -- Sequence
      [:div {:class caption-container-style}
       [Caption
        {:label "Sequence"
         :tooltip "Sequence number for this Account, which is equal to the
         number of transactions that have been executed."}]
       [:code {:class value-style} (if (neg? sequence) "n/a" sequence)]]]


     ;; -- Actions
     [:div.w-full.flex.flex-col.space-y-3
      [Caption
       {:label "Callable Functions"
        :tooltip "Accounts may have functions that any user of the Convex netwtork can call."}]

      (if (seq exports)
        [CallableFunctions account]
        [:span.text-sm.text-gray-500.max-w-prose
         "This Account doesn't have any callable functions."])]


     ;; Environment
     ;; ==============
     [:div.w-full.max-w-prose.flex.flex-col.space-y-2
      [EnvironmentBrowser
       {:convex-web/account account}]

      [:div.pb-20
       [:p.text-sm.text-gray-500.max-w-prose
        "The environment is a space reserved for each Account
       that can freely store on-chain data and definitions.
       (e.g. code that you write in Convex Lisp)"]]]]))

(defn AccountSelect [{:keys [active-address addresses on-change]}]
  (let [state-ref (r/atom {:show? false})]
    (fn [{:keys [active-address addresses on-change]}]
      (let [{:keys [show?]} @state-ref

            item-style ["inline-flex w-full h-16 relative py-2 pl-3 pr-9"
                        "cursor-default select-none"
                        "text-gray-900 text-xs"
                        "hover:bg-blue-100 hover:bg-opacity-50 active:bg-blue-200"]]
        [:div

         ;; -- Selected
         [:button.h-10.inline-flex.items-center.justify-between.cursor-default.w-full.border.border-gray-200.rounded-md.bg-white.text-left.focus:outline-none.focus:shadow-outline-blue.focus:border-blue-300.transition.ease-in-out.duration-150.sm:text-sm.sm:leading-5
          {:on-click #(swap! state-ref update :show? not)}

          (if (str/blank? active-address)
            ;; Empty, but fill the space.
            [:div.flex-1]
            [:div.flex.flex-1.items-center.px-2
             [gui/AIdenticon {:value active-address :size 40}]

             [:span.font-mono.block.ml-2
              (format/prefix-# active-address)]])

          [:svg.h-5.w-5.text-gray-400.pr-2.pointer-events-none
           {:viewBox "0 0 20 20" :fill "none" :stroke "currentColor"}
           [:path {:d "M7 7l3-3 3 3m0 6l-3 3-3-3" :stroke-width "1.5" :stroke-linecap "round" :stroke-linejoin "round"}]]]

         ;; -- Dropdown
         [:div.relative
          [gui/Transition
           (merge gui/dropdown-transition {:show? show?})
           [gui/Dismissible
            {:on-dismiss #(swap! state-ref update :show? (constantly false))}
            [:div.origin-top-right.absolute.right-0.rounded-md.shadow-lg.bg-white
             [:ul.max-h-60.rounded-md.py-1.text-base.leading-6.shadow-xs.overflow-auto.focus:outline-none.sm:text-sm.sm:leading-5

              (for [address addresses]
                ^{:key address}
                [:li
                 {:class item-style
                  :on-click
                  (fn []
                    (reset! state-ref {:show? false})

                    (when on-change
                      (on-change address)))}

                 [:div.flex.items-center
                  [:div.h-5.w-5.mr-2
                   (when (= address active-address)
                     [gui/CheckIcon {:class "h-5 w-5"}])]

                  [gui/AIdenticon {:value address :size 40}]

                  [:span.font-mono.block.ml-2
                   (format/prefix-# address)]]])]]]]]]))))
