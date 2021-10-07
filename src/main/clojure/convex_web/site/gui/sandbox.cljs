(ns convex-web.site.gui.sandbox
  (:require
   [goog.string.format]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]
   [cljfmt.core :as cljfmt]
   [lambdaisland.glogi :as log]

   [convex-web.site.format :as format]
   [convex-web.site.backend :as backend]
   [convex-web.site.gui :as gui]
   [convex-web.site.gui.account :as guia]
   [convex-web.site.command :as command]
   [convex-web.convex :as convex]

   ["@heroicons/react/solid" :as icon]))

(defmulti compile
  "Compiles an AST produced by spec/conform to a Reagent component.

  Currently implemented tags:

    - :text
    - :button
    - :h-box
    - :v-box"
  (comp :tag :ast))

(defmethod compile :default
  [{:keys [ast]}]
  [:code (str ast)])

(defmethod compile :text
  [{:keys [ast]}]
  ;; Text's content is the first number/string/element.
  (let [{:keys [content]} ast

        [_ content-body] (first content)]
    [:span.prose.prose-sm
     (str content-body)]))

(defmethod compile :caption
  [{:keys [ast]}]
  ;; Text's content is the first number/string/element.
  (let [{:keys [content]} ast

        [_ content-body] (first content)]
    [:span.text-xs.text-gray-500
     (str content-body)]))

(defmethod compile :code
  [{:keys [ast]}]
  (let [{:keys [content]} ast

        ;; Code's content is the first number/string/element.
        [_ content-body] (first content)]
    [gui/Highlight
     (try
       (cljfmt/reformat-string (str content-body))
       (catch js/Error _
         (str content-body)))]))

(defmethod compile :button
  [{:keys [ast click-handler] :or {click-handler identity}}]

  (let [{:keys [attributes content]} ast

        {attr-source :source
         attr-action :action} attributes

        ;; Command's content is the first number/string/element.
        [_ content-body] (first content)]

    [:button.p-2.rounded.shadow
     {:class (cond
               (#{:query :transact} attr-action)
               "bg-green-500 hover:bg-green-400 active:bg-green-600"

               (= attr-action :edit)
               "bg-blue-500 hover:bg-blue-400 active:bg-blue-600")
      :on-click
      (fn []
        (let [active-address @(rf/subscribe [:session/?active-address])]
          (cond
            (= attr-action :query)
            (let [command #:convex-web.command {:id (random-uuid)
                                                :timestamp (.getTime (js/Date.))
                                                :status :convex-web.command.status/running
                                                :mode :convex-web.command.mode/query
                                                :query #:convex-web.query {:source attr-source
                                                                           :language :convex-lisp}}]
              (command/execute command
                (fn [_ response]
                  (log/debug :command-new-state response)

                  (rf/dispatch [:session/!set-state
                                (fn [state]
                                  (update-in state [:page.id/repl active-address :convex-web.repl/commands] conj response))]))))

            (= attr-action :edit)
            nil

            :else
            (log/warn :unknown-action attr-action)))

        ;; Caller might need to do something on click,
        ;; especially if it's an edit action.
        (click-handler attributes))}

     ;; Command's button text.
     [:div.flex.justify-start.space-x-2
      [:span.text-sm.text-white
       (str content-body)]

      (cond
        (#{:query :transact} attr-action)
        [:> icon/ArrowRightIcon {:className "w-5 h-5 text-white"}]

        (= :edit attr-action)
        [:> icon/CodeIcon {:className "w-5 h-5 text-white"}]

        :else
        nil)]]))

(defmethod compile :h-box
  [{:keys [ast click-handler]}]
  (let [{:keys [content]} ast]
    (into [:div.flex.flex-row.items-center.space-x-3]
      (map
        (fn [[_ ast]]
          (compile {:ast ast
                    :click-handler click-handler}))
        content))))

(defmethod compile :v-box
  [{:keys [ast click-handler]}]
  (let [{:keys [content]} ast]
    (into [:div.flex.flex-col.items-start.space-y-3]
      (map
        (fn [[_ ast]]
          (compile {:ast ast
                    :click-handler click-handler}))
        content))))


;; End of Interactive Sandbox
;; -----------------------------


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

(defn ResultRenderer [{:keys [result interactive-click-handler]}]
  (let [{result-type :convex-web.result/type
         result-value :convex-web.result/value
         result-metadata :convex-web.result/metadata
         result-interactive? :convex-web.result/interactive?
         result-interactive :convex-web.result/interactive} result]
    (cond
      result-interactive?
      (compile (merge {:ast result-interactive}
                 (when interactive-click-handler
                   {:click-handler interactive-click-handler})))

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
