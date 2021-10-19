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

(defmulti render
  "Render an Interactive Sandbox element.

  Elements are purely data - it's similar to an AST.

  Example:

  {:tag :text
   :content [[:number 1]]}"
  (comp :tag :ast))

(defmethod render :default
  [{:keys [ast]}]
  [:code (str ast)])

(defmethod render :text
  [{:keys [ast]}]
  ;; Text's content is the first number/string/element.
  (let [{:keys [content]} ast

        [_ content-body] (first content)]
    [:span.prose.prose-sm
     (str content-body)]))

(defmethod render :code
  [{:keys [ast]}]
  (let [{:keys [content]} ast

        ;; Code's content is the first number/string/element.
        [_ content-body] (first content)]
    [gui/Highlight
     (try
       (cljfmt/reformat-string (str content-body))
       (catch js/Error _
         (str content-body)))]))

(defmethod render :button
  [{:keys [ast click-handler click-disabled?]}]

  (let [click-handler (or click-handler identity)

        {:keys [attributes content]} ast

        {attr-text :text
         attr-action :action
         :or {attr-action :transact}} attributes

        ;; Button's source is the first value in content.
        ;; (First value in the tuple is the 'type' - string, number, element.)
        [_ source] (first content)

        style (cond
                (#{:query :transact} attr-action)
                ["bg-green-500 hover:bg-green-400 active:bg-green-600"]

                (= attr-action :edit)
                ["bg-blue-500 hover:bg-blue-400 active:bg-blue-600"])

        style (if click-disabled?
                (conj style "disabled:bg-opacity-50 pointer-events-none")
                style)]

    [:button.p-2.rounded.shadow
     {:disabled (or click-disabled? false)
      :class style
      :on-click
      (fn []
        (let [active-address @(rf/subscribe [:session/?active-address])]
          (cond
            (#{:query :transact} attr-action)
            (let [command #:convex-web.command {:id (random-uuid)
                                                :timestamp (.getTime (js/Date.))
                                                :status :convex-web.command.status/running}

                  command (merge command
                            (case attr-action
                              :query
                              #:convex-web.command
                              {:mode :convex-web.command.mode/query
                               :query
                               #:convex-web.query
                               {:source (str source)
                                :language :convex-lisp}}

                              :transact
                              #:convex-web.command
                              {:mode :convex-web.command.mode/transaction
                               :transaction
                               #:convex-web.transaction
                               {:type :convex-web.transaction.type/invoke
                                :source (str source)
                                :language :convex-lisp}}))

                  command (merge command
                            (when active-address
                              {:convex-web.command/signer
                               {:convex-web.account/address active-address}}))]

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
        (click-handler {:ast ast
                        :action attr-action
                        :source source}))}


     [:div.flex.items-center.justify-start.space-x-2

      ;; Button's text.
      (cond
        attr-text
        [:span.text-sm.text-white
         attr-text]

        :else
        [:code.text-xs.text-white
         source])

      ;; Button's icon.
      (cond
        (= :edit attr-action)
        [:> icon/PencilIcon {:className "w-5 h-5 text-white"}]

        :else
        nil)]]))

(defmethod render :markdown
  [{:keys [ast]}]
  (let [{:keys [content]} ast

        [_ content-body] (first content)]
    [:div.prose.prose-sm
     [gui/Markdown
      (str content-body)]]))

(defmethod render :p
  [{:keys [ast click-handler click-disabled?]}]
  (let [{:keys [content]} ast]
    (into [:p.space-x-1.space-y-1]
      (map
        (fn [[_ ast]]
          (render (merge {:ast ast}

                    (when click-handler
                      {:click-handler click-handler})

                    (when click-disabled?
                      {:click-disabled? click-disabled?}))))
        content))))

(defmethod render :h-box
  [{:keys [ast click-handler click-disabled?]}]
  (let [{:keys [content]} ast]
    (into [:div.flex.flex-row.items-center.space-x-3]
      (map
        (fn [[_ ast]]
          (render (merge {:ast ast}

                     (when click-handler
                       {:click-handler click-handler})

                     (when click-disabled?
                       {:click-disabled? click-disabled?}))))
        content))))

(defmethod render :v-box
  [{:keys [ast click-handler click-disabled?]}]
  (let [{:keys [content]} ast]
    (into [:div.flex.flex-col.items-start.space-y-3]
      (map
        (fn [[_ ast]]
          (render (merge {:ast ast}

                     (when click-handler
                       {:click-handler click-handler})

                     (when click-disabled?
                       {:click-disabled? click-disabled?}))))
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

(defn ResultRenderer [{:keys [result interactive]}]
  (let [{result-type :convex-web.result/type
         result-value :convex-web.result/value
         result-metadata :convex-web.result/metadata
         result-interactive :convex-web.result/interactive} result

        {result-interactive? :interactive?} result-metadata

        ;; It's enabled by default.
        interactive-enabled? (get interactive :enabled? true)]

    (cond
      ;; An interactive result is a richer UI, and it's normally used to display tutorials.
      ;;
      ;; A Syntax's metadata is used to 'annotate' the result as interative.
      ;;
      ;; It's possible to disable an interactive result, because its usage is contextual.
      ;; (Doesn't make sense to be used outside the Sandbox.)
      (and result-interactive? interactive-enabled?)
      (render (merge {:ast result-interactive}
                 (select-keys interactive [:click-handler
                                           :click-disabled?])))

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
