(ns convex-web.site.explorer
  (:require [convex-web.site.runtime :refer [sub disp]]
            [convex-web.site.runtime :as runtime]
            [convex-web.site.backend :as backend]
            [convex-web.site.gui :as gui]
            [convex-web.site.stack :as stack]
            [convex-web.site.format :as format]
            [convex-web.pagination :as pagination]
            [convex-web.site.markdown :as markdown]
            [convex-web.site.session :as session]
            [convex-web.glossary :as glossary]

            [clojure.string :as str]
            [cljs.spec.alpha :as s]

            [reitit.frontend.easy :as rfe]
            [reagent.core :as reagent]))

(def blocks-polling-interval 5000)

(defn start-polling-blocks [_ _ set-state]
  (let [interval-callback (fn []
                            (backend/GET-blocks
                              {:handler
                               (fn [blocks]
                                 (set-state #(assoc % :ajax/status :ajax.status/success
                                                      :convex-web/blocks (or blocks []))))
                               :error-handler
                               (fn [error]
                                 (set-state #(assoc % :ajax/status :ajax.status/error
                                                      :ajax/error error)))}))

        interval-ref (runtime/set-interval interval-callback blocks-polling-interval)]
    (set-state #(assoc % :runtime/interval-ref interval-ref
                         :ajax/status :ajax.status/pending))))

(defn stop-polling-blocks [_ {:keys [runtime/interval-ref]} _]
  (when-let [id @interval-ref]
    (runtime/clear-interval id)))

(defn flatten-transactions [blocks]
  (let [block-index&transaction-index (juxt :convex-web.block/index (comp :convex-web.transaction/index :convex-web.signed-data/value))]
    (->> blocks
         (mapcat
           (fn [{:convex-web.block/keys [transactions] :as block}]
             (map-indexed
               (fn [index-transaction signed-data]
                 ;; Add index to Transaction
                 (let [signed-data (assoc-in signed-data [:convex-web.signed-data/value :convex-web.transaction/index] index-transaction)]
                   (merge block signed-data)))
               transactions)))
         (sort-by block-index&transaction-index)
         (reverse))))

(s/fdef flatten-transactions
  :args (s/cat :blocks :convex-web/blocks)
  :ret (s/merge :convex-web/block :convex-web/signed-data))


(defn CodePage [_ {:keys [source]} _]
  [:div.p-4
   [gui/Highlight source {:pretty? true}]])

(def code-page
  #:page {:id :page.id/code
          :component #'CodePage})


(defn TransactionPage [_ state _]
  (let [{:keys [convex-web.block/index
                convex-web.block/timestamp
                convex-web.signed-data/address
                convex-web.signed-data/value]} state


        {:convex-web.transaction/keys [type source sequence result]} value]
    [:div.flex.flex-col.space-y-8.p-6

     ;; Header
     ;; ======================
     [:div.flex.space-x-10.bg-gray-100.p-6.rounded.shadow

      ;; -- Block
      [:div.flex.flex-col.space-y-2.text-right
       [gui/CaptionMono "Block"]
       [gui/Tooltip
        {:title glossary/block-number}
        [:a
         {:class gui/hyperlink-hover-class
          :href (rfe/href :route-name/block-explorer {:index index})}
         [:span.text-sm
          index]]]]

      ;; -- TR#
      [:div.flex.flex-col.space-y-2.text-right
       [gui/CaptionMono "TR#"]
       [gui/Tooltip
        {:title glossary/transaction-index}
        [:span.text-sm.cursor-default
         (:convex-web.transaction/index value)]]]

      ;; -- Signer
      [:div.flex.flex-col.space-y-2
       [gui/CaptionMono "Signer"]

       [:div.flex.items-center.w-40
        [gui/AIdenticon {:value address :size gui/identicon-size-small}]

        [:a.flex-1.truncate
         {:class gui/hyperlink-hover-class
          :href (rfe/href :route-name/account-explorer {:address address})}
         [gui/Tooltip
          {:title address}
          [:span.font-mono.text-xs (format/prefix-# address)]]]]]

      ;; -- Timestamp
      [:div.flex.flex-col.space-y-2
       [gui/CaptionMono "Timestamp"]
       (let [timestamp (-> timestamp
                           (format/date-time-from-millis)
                           (format/date-time-to-string))]
         [gui/Tooltip
          {:title timestamp}
          [:span.text-sm.cursor-default
           (format/time-ago timestamp)]])]

      ;; -- Type
      [:div.flex.flex-col.space-y-2
       [gui/CaptionMono "Type"]
       [gui/Tooltip
        {:title (gui/transaction-type-description type)}
        [:span.text-sm.uppercase.cursor-default
         {:class (gui/transaction-type-text-color type)}
         type]]]

      ;; -- Sequence Number
      [:div.flex.flex-col.space-y-2.text-right
       [gui/CaptionMono "Sequence Number"]
       [gui/Tooltip
        {:title glossary/sequence-number}
        [:span.text-sm.uppercase.cursor-default
         sequence]]]

      ;; -- Status
      [:div.flex.flex-col.space-y-2
       [gui/CaptionMono "Status"]
       [gui/Tooltip
        {:title glossary/transaction-status}

        (if (get result :convex-web.result/error-code)
          [:span.text-sm.uppercase.cursor-default.text-red-500 "Error"]
          [:span.text-sm.uppercase.cursor-default "OK"])]]]

     ;; Source
     ;; ======================
     (case type
       :convex-web.transaction.type/invoke
       [:div.flex.flex-col.space-y-2
        [gui/CaptionMono "Source"]
        [gui/Highlight source {:pretty? true}]]

       :convex-web.transaction.type/transfer
       [:div])


     ;; Result
     ;; ======================
     (case type
       :convex-web.transaction.type/invoke
       (let [{result-value :convex-web.result/value
              result-value-kind :convex-web.result/value-kind
              result-error-code :convex-web.result/error-code} result]
         [:div.flex.flex-col.space-y-2
          [:div.flex.space-x-1
           [gui/CaptionMono (if result-error-code "Error" "Result")]

           ;; -- Meta/Kind
           (when-not result-error-code
             (when result-value-kind
               [gui/InfoTooltip (str/capitalize (name result-value-kind))]))]

          (if result-error-code
            [:span.font-mono.text-sm.text-red-500 result-error-code ": " result-value]
            [gui/ObjectRenderer result-value result-value-kind])])

       :convex-web.transaction.type/transfer
       [:div])


     ;; Trace
     ;; ======================
     (when-let [trace (seq (:convex-web.result/trace result))]
       [:div.flex.flex-col.space-y-2
        [gui/CaptionMono "Trace"]

        (for [t trace]
          ^{:key t}
          [:span.font-mono.text-sm t])])]))

(def transaction-page
  #:page {:id :page.id/transaction
          :component #'TransactionPage
          :title "Transaction"
          :state-spec (s/merge :convex-web/block :convex-web/signed-data)})

(defn TransactionsTable [blocks]
  [:div
   [:table.text-left.table-auto
    [:thead
     (let [th-style "text-xs uppercase text-gray-600 sticky top-0 cursor-default"
           th-div-style "py-2 mr-8"]
       [:tr
        ;; -- 0. Block
        [:th
         {:class th-style}
         [:div.flex.space-x-1
          {:class th-div-style}
          [:span "Block"]
          [gui/InfoTooltip glossary/block-number]]]

        ;; -- 1. TR#
        [:th
         {:class th-style}
         [:div.flex.space-x-1
          {:class th-div-style}
          [:span "TR#"]
          [gui/InfoTooltip glossary/transaction-index]]]

        ;; -- 2. Signer
        [:th
         {:class th-style}
         [:div.flex.space-x-1
          {:class th-div-style}
          [:span "Signer"]
          [gui/InfoTooltip "Address of the Account that digitally signed the transaction. This Signature has been verified by all Peers in Consensus."]]]

        ;; -- 3. Timestamp
        [:th
         {:class th-style}
         [:div.flex.space-x-1
          {:class th-div-style}
          [:span "Timestamp"]
          [gui/InfoTooltip "UTC Timestamp of the block containing the transaction"]]]

        ;; -- 4. Type
        [:th
         {:class th-style}
         [:div.flex.space-x-1
          {:class th-div-style}
          [:span "Type"]
          [gui/InfoTooltip
           "Transfer: Direct transfer of Convex Coins from the Signer's
            Account to a destination Account; Invoke: Execution of code by
            Signer Account"]]]

        ;; -- 5. Sequence Number
        [:th
         {:class th-style}
         [:div.flex.space-x-1
          {:class th-div-style}
          [:span "Sequence Number"]
          [gui/InfoTooltip glossary/sequence-number]]]

        ;; -- Status
        [:th
         {:class th-style}
         [:div.flex.space-x-1
          {:class th-div-style}
          [:span "Status"]
          [gui/InfoTooltip
           glossary/transaction-status]]]

        ;; -- 7. Result
        [:th
         {:class th-style}
         [:div.flex.space-x-1
          {:class th-div-style}
          [:span "Result"]
          [gui/InfoTooltip
           "Transfer: Amount and destination Address; Invoke: Convex Lisp or
            Scrypt code executed on the CVM for the transaction."]]]])]

    [:tbody
     (for [m (flatten-transactions blocks)]
       (let [block-index (get m :convex-web.block/index)

             {transaction-index :convex-web.transaction/index
              transaction-type :convex-web.transaction/type
              transaction-sequence :convex-web.transaction/sequence
              transaction-result :convex-web.transaction/result} (get m :convex-web.signed-data/value)

             td-class ["p-1 whitespace-no-wrap text-xs"]]
         ^{:key [block-index transaction-index]}
         [:tr.cursor-default
          ;; -- 0. Block Index
          [:td {:class td-class}
           [:div.flex.flex-1.justify-end
            [:a
             {:class gui/hyperlink-hover-class
              :href (rfe/href :route-name/block-explorer {:index block-index})}
             [:span.font-mono block-index]]]]

          ;; -- 1. TR#
          [:td {:class (cons "text-right" td-class)}
           [:span.text-xs.mr-8
            transaction-index]]

          ;; -- 2. Signer
          [:td {:class td-class}
           (let [address (get m :convex-web.signed-data/address)]
             [:div.flex.items-center.w-40
              [gui/AIdenticon {:value address :size gui/identicon-size-small}]

              [:a.flex-1.truncate
               {:class gui/hyperlink-hover-class
                :href (rfe/href :route-name/account-explorer {:address address})}
               [gui/Tooltip
                {:title (str "Account " (format/prefix-# address))
                 :size "small"}
                [:span.font-mono.text-xs (format/prefix-# address)]]]])]

          ;; -- 3. Timestamp
          [:td {:class td-class}
           (let [timestamp (-> (get m :convex-web.block/timestamp)
                               (format/date-time-from-millis)
                               (format/date-time-to-string))]
             [gui/Tooltip
              {:title timestamp}
              [:span (format/time-ago timestamp)]])]

          ;; -- 4. Type
          [:td
           {:class
            (conj td-class (case transaction-type
                             :convex-web.transaction.type/transfer
                             "text-indigo-500"

                             :convex-web.transaction.type/invoke
                             "text-pink-500"

                             ""))}
           [gui/Tooltip
            (case transaction-type
              :convex-web.transaction.type/transfer
              "Direct transfer of Convex Coins from the Signer's Account to a destination Account"

              :convex-web.transaction.type/invoke
              "Execution of code by Signer Account"

              "")
            [:span.text-xs.uppercase
             transaction-type]]]

          ;; -- 5. Sequence Number
          [:td
           {:class (conj td-class "text-right")}
           [:span.text-xs.uppercase.mr-8
            transaction-sequence]]

          ;; -- 6. Status
          [:td
           {:class td-class}
           (if (get transaction-result :convex-web.result/error-code)
             [:span.text-xs.text-red-500 "ERROR"]
             [:span.text-xs "OK"])]

          ;; -- 7. Value
          [:td
           {:class td-class}
           (case (get-in m [:convex-web.signed-data/value :convex-web.transaction/type])
             :convex-web.transaction.type/invoke
             [gui/SecondaryButton
              {:on-click #(stack/push :page.id/transaction {:state m
                                                            :modal? true})}
              [gui/ButtonText
               {:padding gui/button-child-small-padding
                :text-size "text-xs"
                :text-transform "normal-case"}
               "View details"]]

             :convex-web.transaction.type/transfer
             [:span.inline-flex.items-center
              [:span.mr-1 "Transferred"]

              [:span.font-bold.text-indigo-500.mr-1
               (format/format-number
                 (get-in m [:convex-web.signed-data/value :convex-web.transaction/amount]))]

              [:span.mr-1 " to "]

              (let [address (get-in m [:convex-web.signed-data/value :convex-web.transaction/target])]
                [:div.flex.items-center.w-40
                 [gui/AIdenticon {:value address :size gui/identicon-size-small}]

                 [:a.flex-1.truncate
                  {:class gui/hyperlink-hover-class
                   :href (rfe/href :route-name/account-explorer {:address address})}
                  [gui/Tooltip
                   {:title (str "Account " (format/prefix-# address))
                    :size "small"}
                   [:span.font-mono.text-xs (format/prefix-# address)]]]])])]]))]]])

(s/def :explorer.blocks.state/pending
  (s/merge :ajax/pending-status (s/keys :req [:runtime/interval-ref])))

(s/def :explorer.blocks.state/success
  (s/merge :ajax/success-status (s/keys :req [:runtime/interval-ref :convex-web/blocks])))

(s/def :explorer.blocks.state/error
  (s/merge :ajax/error-status (s/keys :req [:runtime/interval-ref])))

(s/def :explorer.blocks/state-spec
  (s/or :pending :explorer.blocks.state/pending
        :success :explorer.blocks.state/success
        :error :explorer.blocks.state/error))

(s/def :explorer.blocks-range/state-spec
  (s/nonconforming
    (s/or :pending :ajax/pending-status
          :success (s/merge :ajax/success-status (s/keys :req [:convex-web/blocks]))
          :error :ajax/error-status)))

;; ---

(defn Block [{:convex-web.block/keys [index] :as block}]
  [:div.flex.flex-col.space-y-8

   ;; -- Index
   [:div.flex.flex-col.items-start.space-y-2
    [gui/Caption "Index"]
    [:span.text-4xl.text-center.text-gray-700.leading-none index]]

   ;; -- Transactions
   [:div.flex.flex-col.space-y-2
    [gui/Caption "Transactions"]
    [TransactionsTable (vector block)]]])

(defn BlockPage [_ {:keys [ajax/status ajax/error convex-web/block]} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-1.justify-center.items-center
     [gui/Spinner]]

    :ajax.status/success
    [Block block]

    :ajax.status/error
    [:span (get-in error [:response :error :message])]

    ;; Fallback
    [:div]))


(s/def :block-page.state/pending (s/merge :ajax/pending-status (s/keys :req [:convex-web/block])))
(s/def :block-page.state/success (s/merge :ajax/success-status (s/keys :req [:convex-web/block])))
(s/def :block-page.state/error :ajax/error-status)
(s/def :block-page/state-spec (s/or :pending :block-page.state/pending
                                    :success :block-page.state/success
                                    :error :block-page.state/error))

(defn- get-block [_ state set-state]
  (set-state assoc :ajax/status :ajax.status/pending)

  (let [index (get-in state [:convex-web/block :convex-web.block/index])]
    (backend/GET-block
      index
      {:handler
       (fn [block]
         (when block
           (set-state #(assoc % :convex-web/block block
                                :ajax/status :ajax.status/success))))
       :error-handler
       (fn [error]
         (set-state #(assoc % :ajax/status :ajax.status/error
                              :ajax/error error)))})))

(def block-page
  #:page {:id :page.id/block-explorer
          :title "Block"
          :component #'BlockPage
          :state-spec :block-page/state-spec
          :on-push #'get-block})

;; --- Account

(defn AccountPage [_ {:keys [convex-web/account ajax/status ajax/error]} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-col.flex-1.justify-center.items-center
     [gui/Spinner]]

    :ajax.status/error
    [:div.flex.flex-col.flex-1.justify-center.items-center
     [:span (get-in error [:response :error :message])]]

    :ajax.status/success
    [gui/Account account]))

(defn- get-account [_ state set-state]
  (let [address (get-in state [:convex-web/account :convex-web.account/address])]
    (backend/GET-account
      address
      {:handler
       (fn [account]
         (set-state #(assoc % :convex-web/account account
                              :ajax/status :ajax.status/success)))
       :error-handler
       (fn [error]
         (set-state #(assoc % :ajax/status :ajax.status/error
                              :ajax/error error)))})))

(s/def :account-page.state/pending (s/and (s/keys :req [:ajax/status :convex-web/account])
                                          #(= :ajax.status/pending (:ajax/status %))))

(s/def :account-page.state/success (s/and (s/keys :req [:ajax/status :convex-web/account])
                                          #(= :ajax.status/success (:ajax/status %))))

(s/def :account-page.state/error (s/and (s/keys :req [:ajax/status :ajax/error])
                                        #(= :ajax.status/error (:ajax/status %))))

(s/def :account-page/state-spec (s/or :pending :account-page.state/pending
                                      :success :account-page.state/success
                                      :error :account-page.state/error))

(def account-page
  #:page {:id :page.id/account-explorer
          :title "Account"
          :state-spec :account-page/state-spec
          :component #'AccountPage
          :on-push #'get-account})


;; -- Accounts Commons

(defn AccountsTable [accounts & [{:keys [modal?]}]]
  [:div
   [:table.text-left.table-auto
    [:thead
     (let [th-class "text-xs uppercase text-gray-600 sticky top-0 pr-2"]
       [:tr
        [:th
         [:div.flex.space-x-1
          {:class th-class}
          [:span "Address"]]]

        [:th
         [:div.flex.space-x-1
          {:class th-class}
          [:span "Type"]
          [gui/InfoTooltip
           "The Type of Account, may be User, Actor or Library"]]]

        [:th
         [:div.flex.space-x-1
          {:class th-class}
          [:span "Balance"]
          [gui/InfoTooltip
           "Account Balance denominated in Convex Copper Coins (the smallest coin unit)"]]]

        [:th
         [:div.flex.space-x-1
          {:class th-class}
          [:span "Memory Size"]
          [gui/InfoTooltip
           "Size in bytes of this Account, which includes any definitions you
            have created in your Enviornment."]]]

        [:th
         [:div.flex.space-x-1
          {:class th-class}
          [:span "Memory Allowance"]
          [gui/InfoTooltip
           "Reserved Memory Allowance in bytes. If you create on-chain data
            beyond this amount, you will be charged extra transaction fees to
            aquire memory at the current memory pool price."]]]])]

    (let [active-address (session/?active-address)

          my-addresses (->> (session/?accounts)
                            (map :convex-web.account/address)
                            (into #{}))]
      [:tbody
       (for [{:convex-web.account/keys [address status]} (sort-by :convex-web.account/address accounts)]
         (let [td-class "p-2 font-mono text-xs text-gray-700 whitespace-no-wrap"

               me? (contains? my-addresses address)

               address-string (format/prefix-# address)]
           ^{:key address}
           [:tr.cursor-default
            ;; -- Address
            [:td.flex.items-center {:class td-class}
             [:div.flex.items-center
              [gui/AIdenticon {:value address :size 28}]

              (if modal?
                [:code.underline.cursor-pointer.mx-2
                 {:on-click #(stack/push :page.id/account-explorer {:state
                                                                    {:ajax/status :ajax.status/pending
                                                                     :convex-web/account {:convex-web.account/address address}}
                                                                    :modal? true})}
                 address-string]
                [:a.flex-1.mx-2
                 {:class gui/hyperlink-hover-class
                  :href (rfe/href :route-name/account-explorer {:address address})}
                 [:code.text-xs address-string]])]

             (when (and me? (not= address active-address))
               [gui/Tooltip
                {:title "Switch to this account"}
                [:span.uppercase.text-gray-500.hover:text-black.ml-4.mr-4.cursor-pointer
                 {:on-click #(session/pick-address address)}
                 "Switch"]])

             (when (and me? (= address active-address))
               [gui/Tooltip
                {:title "This account is active"}
                [gui/CheckIcon {:class "w-4 h-4 text-green-500 ml-4"}]])]


            ;; -- Type
            [:td {:class td-class}
             [gui/Tooltip
              {:title (gui/account-type-description status)}
              [:div.flex-1.px-2.rounded
               {:class (gui/account-type-text-color status)}
               [:span.uppercase
                (gui/account-type-label status)]]]]

            ;; -- Balance
            [:td {:class td-class}
             [:div.flex.justify-end
              [:span.text-xs.font-bold.text-indigo-500
               (format/format-number (str (:convex-web.account-status/balance status)))]]]

            ;; -- Memory size
            [:td {:class td-class}
             [:div.flex.justify-end
              [:span.text-xs.font-bold.text-indigo-500
               (format/format-number (str (:convex-web.account-status/memory-size status)))]]]

            ;; -- Memory allowance
            [:td {:class td-class}
             [:div.flex.justify-end
              [:span.text-xs.font-bold.text-indigo-500
               (format/format-number (str (:convex-web.account-status/allowance status)))]]]]))])]])

(defn- get-accounts-range [{:keys [start end]} set-state]
  (backend/GET-accounts
    (merge {:handler
            (fn [{:keys [meta convex-web/accounts]}]
              (set-state assoc
                         :ajax/status :ajax.status/success
                         :convex-web/accounts (or accounts [])
                         :meta meta))
            :error-handler
            (fn [error]
              (set-state #(assoc % :ajax/status :ajax.status/error
                                   :ajax/error error)))}
           (when start
             {:start start})
           (when end
             {:end end}))))

;; -- Accounts Range

(defn AccountsRangePage [{:frame/keys [modal?]} {:keys [ajax/status convex-web/accounts meta]} set-state]
  (let [{:keys [start end total] :as range} meta

        {start-previous-range :start end-previous-range :end :as previous-range} (pagination/increase-range end total)

        previous-query (if (= start-previous-range end-previous-range)
                         {}
                         previous-range)

        {start-next-range :start end-next-range :end :as next-range} (pagination/decrease-range start)

        next-query (if (= start-next-range end-next-range)
                     pagination/min-range
                     next-range)]
    [:div.flex.flex-col.flex-1.space-y-2

     ;; -- Pagination
     [gui/RangeNavigation
      (merge range {:page-count (pagination/page-count total)
                    :page-num (pagination/page-num start total)
                    :first-label "First"
                    :first-href (rfe/href :route-name/accounts-explorer)
                    :last-label "Last"
                    :last-href (rfe/href :route-name/accounts-explorer {} pagination/min-range)
                    :previous-href (rfe/href :route-name/accounts-explorer {} previous-query)
                    :next-href (rfe/href :route-name/accounts-explorer {} next-query)
                    :ajax/status status})]

     ;; -- Body
     (case status
       :ajax.status/pending
       [:div.flex.flex-1.justify-center.items-center
        [gui/Spinner]]

       [AccountsTable accounts {:modal? modal?}])]))

(def accounts-range-page
  #:page {:id :page.id/accounts-range-explorer
          :title "Accounts"
          :component #'AccountsRangePage
          :state-spec :accounts-page/state-spec
          :on-push
          (fn [_ state set-state]
            (set-state assoc :ajax/status :ajax.status/pending)

            (get-accounts-range state set-state))})


;; -- Accounts

(defn AccountsPage [{:frame/keys [modal?]} {:keys [ajax/status convex-web/accounts]} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-1.justify-center.items-center
     [gui/Spinner]]

    [:div.flex.flex-col
     [AccountsTable accounts {:modal? modal?}]

     [:div.my-4
      [gui/Tooltip
       "View all Accounts"
       [gui/DefaultButton
        {:on-click #(stack/push :page.id/accounts-range-explorer {:modal? true})}
        [:span.text-xs.uppercase "View All"]]]]]))

(s/def :accounts-page.state/pending (s/and (s/keys :req [:ajax/status])
                                           #(= :ajax.status/pending (:ajax/status %))))

(s/def :accounts-page.state/success (s/and (s/keys :req [:ajax/status :convex-web/accounts])
                                           #(= :ajax.status/success (:ajax/status %))))

(s/def :accounts-page.state/error (s/and (s/keys :req [:ajax/status :ajax/error])
                                         #(= :ajax.status/error (:ajax/status %))))

(s/def :accounts-page/state-spec (s/or :pending :accounts-page.state/pending
                                       :success :accounts-page.state/success
                                       :error :accounts-page.state/error))

(def accounts-page
  #:page {:id :page.id/accounts-explorer
          :title "Accounts"
          :state-spec :accounts-page/state-spec
          :component #'AccountsPage
          :on-push
          (fn [_ state set-state]
            (set-state assoc :ajax/status :ajax.status/pending)

            (get-accounts-range state set-state))
          :on-resume
          (fn [_ state set-state]
            (get-accounts-range state set-state))})

;; -- Blocks

(defn BlocksTable [blocks]
  (let [sorting-ref (reagent/atom {:keyfn :convex-web.block/index
                                   :ascending? false})]
    (fn [blocks]
      (let [{:keys [keyfn ascending?]} @sorting-ref

            {:keys [SortIcon comparator]} (if ascending?
                                            {:SortIcon gui/SortAscendingIcon
                                             :comparator #(compare %1 %2)}
                                            {:SortIcon gui/SortDescendingIcon
                                             :comparator #(compare %2 %1)})

            blocks (sort-by keyfn comparator blocks)

            SortableColumn (fn [{keyfn' :keyfn label :label tooltip :tooltip}]
                             [:div.flex.space-x-4.p-2.hover:bg-gray-200.cursor-default
                              {:on-click #(reset! sorting-ref {:keyfn keyfn'
                                                               :ascending? (not ascending?)})}
                              [:div.flex.space-x-1
                               [:span label]
                               [gui/InfoTooltip tooltip]]

                              [SortIcon {:class ["w-4 h-4 ml-1" (when-not (= keyfn' keyfn)
                                                                  "invisible")]}]])]
        [:div
         [:table.text-left.table-auto
          [:thead
           (let [th-class "text-xs uppercase text-gray-600 sticky top-0"]
             [:tr.select-none
              [:th {:class th-class}
               [SortableColumn
                {:label "Index"
                 :tooltip "Block number, indicating the position of the block in the consensus ordering."
                 :keyfn :convex-web.block/index}]]

              [:th
               {:class th-class}
               [SortableColumn
                {:label "Timestamp"
                 :tooltip "UTC Timestamp of the block, as declared by the publishing Peer"
                 :keyfn :convex-web.block/timestamp}]]

              [:th
               {:class th-class}
               [SortableColumn
                {:label "Peer"
                 :tooltip "Address of the Peer on the Convex network that published the block (e.g. the convex.world Server)"
                 :keyfn :convex-web.block/peer}]]])]

          [:tbody
           (for [{:convex-web.block/keys [index peer timestamp] :as block} blocks]
             (let [td-class ["text-xs text-gray-700 whitespace-no-wrap px-2"]]
               ^{:key index}
               [:tr.cursor-default
                ;; -- Index
                [:td {:class td-class}
                 [:div.flex.flex-1.justify-end
                  [:a
                   {:href (rfe/href :route-name/block-explorer {:index index})
                    :class gui/hyperlink-hover-class}
                   [:span.font-mono index]]]]

                ;; -- Timestamp
                [:td {:class td-class}
                 (let [utc-time (-> timestamp
                                    (format/date-time-from-millis)
                                    (format/date-time-to-string))]
                   [gui/Tooltip
                    {:title utc-time}
                    [:span (format/time-ago utc-time)]])]

                ;; -- Peer
                [:td {:class td-class}
                 [:div.flex.items-center.space-x-1
                  [gui/Jdenticon {:value peer :size gui/identicon-size-small}]
                  [:span (format/prefix-# peer)]]]]))]]]))))

(defn BlocksPage [{:frame/keys [modal?]} {:keys [ajax/status convex-web/blocks]} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-1.justify-center.items-center
     [gui/Spinner]]

    [:div.flex.flex-col.flex-1.items-start
     [BlocksTable blocks {:modal? modal?}]

     [:div.my-4
      [gui/Tooltip
       "View all Blocks"
       [gui/DefaultButton
        {:on-click #(stack/push :page.id/blocks-range-explorer {:modal? true})}
        [:span.text-xs.uppercase "View All"]]]]]))

(def blocks-page
  #:page {:id :page.id/blocks-explorer
          :title "Latest Blocks"
          :component #'BlocksPage
          :state-spec :explorer.blocks/state-spec
          :on-push #'start-polling-blocks
          :on-pop #'stop-polling-blocks})


;; -- Blocks Range

(defn get-blocks-range [{:keys [start end set-state]}]
  (set-state assoc :ajax/status :ajax.status/pending)

  (backend/GET-blocks-range
    (merge {:handler
            (fn [{:keys [meta convex-web/blocks]}]
              (set-state #(assoc % :ajax/status :ajax.status/success
                                   :convex-web/blocks blocks
                                   :meta meta)))
            :error-handler
            (fn [error]
              (set-state #(assoc % :ajax/status :ajax.status/error
                                   :ajax/error error)))}
           (when start
             {:start start})
           (when end
             {:end end}))))

(defn BlocksRangePage [_ {:keys [ajax/status convex-web/blocks meta]} _]
  [:div.flex.flex-col.flex-1.space-y-2

   ;; -- Pagination
   (let [{:keys [start end total] :as range} meta

         {start-previous-range :start end-previous-range :end :as previous-range} (pagination/increase-range end total)

         previous-query (if (= start-previous-range end-previous-range)
                          {}
                          previous-range)

         {start-next-range :start end-next-range :end :as next-range} (pagination/decrease-range start)

         next-query (if (= start-next-range end-next-range)
                      pagination/min-range
                      next-range)]

     [gui/RangeNavigation
      (merge range {:page-count (pagination/page-count total)
                    :page-num (pagination/page-num start total)
                    :first-href (rfe/href :route-name/blocks)
                    :last-href (rfe/href :route-name/blocks {} pagination/min-range)
                    :previous-href (rfe/href :route-name/blocks {} previous-query)
                    :next-href (rfe/href :route-name/blocks {} next-query)
                    :ajax/status status})])

   ;; -- Body
   (case status
     :ajax.status/pending
     [:div.flex.flex-1.justify-center.items-center
      [gui/Spinner]]

     [BlocksTable blocks {:modal? true}])])

(defn get-blocks-on-push [_ state set-state]
  (get-blocks-range (merge state {:set-state set-state})))

(def blocks-range-page
  #:page {:id :page.id/blocks-range-explorer
          :title "Blocks"
          :description "These are the Blocks that have been published in the
                        Convex Network Consensus, numbered in consensus order
                        (higher index numbers are more recent)."
          :component #'BlocksRangePage
          :state-spec :explorer.blocks-range/state-spec
          :on-push #'get-blocks-on-push})


;; -- Peers

(defn PeersPage [_ _ _]
  [:div.flex.flex-1.justify-center.items-center
   [:span.text-4xl "TODO"]])

(def peers-page
  #:page {:id :page.id/peers-explorer
          :title "Peers"
          :component #'PeersPage})


;; -- Transactions

(defn TransactionsRangePage [_ {:keys [ajax/status convex-web/blocks meta]} _]
  [:div.flex.flex-col.flex-1.space-y-2

   ;; -- Pagination
   (let [{:keys [start end total] :as range} meta

         {start-previous-range :start end-previous-range :end :as previous-range} (pagination/increase-range end total)

         previous-query (if (= start-previous-range end-previous-range)
                          {}
                          previous-range)

         {start-next-range :start end-next-range :end :as next-range} (pagination/decrease-range start)

         next-query (if (= start-next-range end-next-range)
                      pagination/min-range
                      next-range)]
     [gui/RangeNavigation
      (merge range {:page-count (pagination/page-count total)
                    :page-num (pagination/page-num start total)
                    :first-href (rfe/href :route-name/transactions)
                    :last-href (rfe/href :route-name/transactions {} pagination/min-range)
                    :previous-href (rfe/href :route-name/transactions {} previous-query)
                    :next-href (rfe/href :route-name/transactions {} next-query)
                    :ajax/status status})])

   ;; -- Body
   (case status
     :ajax.status/pending
     [:div.flex.flex-1.justify-center.items-center
      [gui/Spinner]]

     [TransactionsTable (or blocks [])])])

(def transactions-range-page
  #:page {:id :page.id/transactions-range-explorer
          :title "Transactions"
          :component #'TransactionsRangePage
          :state-spec :explorer.blocks-range/state-spec
          :on-push #'get-blocks-on-push})

(defn TransactionsPage [_ {:keys [ajax/status convex-web/blocks]} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-1.justify-center.items-center
     [gui/Spinner]]

    [:div.flex.flex-col.flex-1
     [TransactionsTable (or blocks [])]

     [:div.my-4
      [gui/Tooltip
       "View all Transactions"
       [gui/DefaultButton
        {:on-click #(stack/push :page.id/transactions-range-explorer {:modal? true})}
        [:span.text-xs.uppercase "View All"]]]]]))

(def transactions-page
  #:page {:id :page.id/transactions-explorer
          :title "Latest Transactions"
          :component #'TransactionsPage
          :state-spec :explorer.blocks/state-spec
          :on-push #'start-polling-blocks
          :on-pop #'stop-polling-blocks})
