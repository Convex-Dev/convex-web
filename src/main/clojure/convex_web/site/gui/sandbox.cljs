(ns convex-web.site.gui.sandbox
  (:require
   [clojure.string :as str]

   [goog.string.format]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]
   [cljfmt.core :as cljfmt]
   [lambdaisland.glogi :as log]
   [codemirror-reagent.core :as codemirror]

   [convex-web.site.format :as format]
   [convex-web.site.backend :as backend]
   [convex-web.site.gui :as gui]
   [convex-web.site.gui.account :as guia]
   [convex-web.site.command :as command]
   [convex-web.convex :as convex]))

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

(defn CommandRenderer
  "A Command Widget is used to run queries and transactions on chain.

  A button is used to run the command."
  [{:keys [ast]}]
  (r/with-let [source-ref (r/atom (get-in ast [:content 0 1]))

               input-ref (r/atom {})]
    (let [{:keys [attributes]} ast

          {cmd-name :name
           cmd-mode :mode
           cmd-show-source? :show-source?
           cmd-lang :lang
           cmd-input :input
           :or {cmd-mode :transact}} attributes

          source @source-ref

          active-address @(rf/subscribe [:session/?active-address])

          ;; Execute command, query or transaction, and update state.
          execute (fn []
                    (cond
                      (#{:query :transact} cmd-mode)
                      (let [source (if cmd-lang
                                     ;; A lang function is given a form (quoted) and input (a map).
                                     (str "(" cmd-lang " '" source " " @input-ref ")")
                                     source)

                            _ (js/console.log source)

                            command #:convex-web.command {:id (random-uuid)
                                                          :timestamp (.getTime (js/Date.))
                                                          :status :convex-web.command.status/running}

                            ;; Merge Command header and source.
                            command (merge command
                                      (case cmd-mode
                                        :query
                                        #:convex-web.command
                                        {:mode :convex-web.command.mode/query
                                         :query
                                         #:convex-web.query
                                         {:source source
                                          :language :convex-lisp}}

                                        :transact
                                        #:convex-web.command
                                        {:mode :convex-web.command.mode/transaction
                                         :transaction
                                         #:convex-web.transaction
                                         {:type :convex-web.transaction.type/invoke
                                          :source source
                                          :language :convex-lisp}}))

                            ;; Merge Command and signer (optional).
                            command (merge command
                                      (when active-address
                                        {:convex-web.command/signer
                                         {:convex-web.account/address active-address}}))]

                        (command/execute command
                          (fn [_ response]
                            (let [{:keys [cls? input mode]} (get-in response [:convex-web.command/result :convex-web.result/metadata])

                                  f (fn [state]
                                      (let [state (if cls?
                                                    ;; Reset history (since it's a 'clear screen')
                                                    (assoc-in state [:page.id/repl active-address :convex-web.repl/commands] [response])
                                                    ;; Append command.
                                                    (update-in state [:page.id/repl active-address :convex-web.repl/commands] conj response))]

                                        ;; Set source from syntax's metadata.
                                        (when-not (str/blank? input)
                                          (codemirror/cm-set-value (:editor state) input))

                                        ;; Overwrite mode from syntax's metadata.
                                        (cond-> state
                                          mode
                                          (assoc :convex-web.repl/mode mode))))]


                              (rf/dispatch [:session/!set-state f])))))

                      :else
                      (log/warn :unknown-mode cmd-mode)))]

      ;; Container for inline editor and button.
      [:div.inline-flex.flex-col.items-start.space-y-2

       (when cmd-input
         [:div.flex.flex-col.space-y-2
          (for [[id {input-type :type}]cmd-input]
            ^{:key id}
            [:div.flex.flex-col.space-y-1.bg-blue-100.p-1.rounded.shadow

             [:span.p-1.bg-blue-50.text-gray-700.rounded
              (name id)]

             [:input.p-1.rounded
              {:type (or input-type "text")
               :on-change
               (fn [e]
                 (let [v (gui/event-target-value e)

                       v (cond
                           (= input-type "number")
                           (js/parseFloat v)

                           :else
                           v)]

                   (swap! input-ref assoc id v)))}]])])

       ;; Inline editor for Command.
       (when cmd-show-source?
         [codemirror/CodeMirror
          [:div.relative.flex-shrink-0.flex-1.overflow-auto.border.rounded]
          {:configuration {:lineNumbers false
                           :value source
                           :mode "clojure"}

           :on-mount (fn [_ editor]
                       (codemirror/cm-focus editor))

           :on-update (fn [_ editor]
                        (codemirror/cm-focus editor))

           :events {:editor
                    {"change"
                     (fn [editor _]
                       (reset! source-ref (codemirror/cm-get-value editor)))}}}])

       ;; Command is executed on (button) click.
       [:button.px-1.rounded.shadow
        {:class ["bg-green-500 hover:bg-green-400 active:bg-green-600"]
         :on-click execute}
        (cond
          cmd-name
          [:span.text-sm.text-white
           cmd-name]

          :else
          [:code.text-xs.text-white
           source])]])))

(defmethod render :cmd [m]
  [CommandRenderer m])

(defmethod render :md
  [{:keys [ast]}]
  (let [{:keys [content]} ast

        [_ content-body] (first content)]
    [:div.prose.prose-sm
     [gui/Markdown
      (str content-body)]]))

(defmethod render :p
  [{:keys [ast]}]
  (let [{:keys [content]} ast]
    (into [:p]
      (map
        (fn [[_ ast]]
          (render {:ast ast}))
        content))))

(defmethod render :h-box
  [{:keys [ast]}]
  (let [{:keys [content]} ast]
    (into [:div.flex.flex-row.items-center.space-x-3]
      (map
        (fn [[_ ast]]
          (render {:ast ast}))
        content))))

(defmethod render :v-box
  [{:keys [ast]}]
  (let [{:keys [content]} ast]
    (into [:div.flex.flex-col.items-start.space-y-3]
      (map
        (fn [[_ ast]]
          (render {:ast ast}))
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

        {result-interactive? :interact?} result-metadata

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
      (render {:ast result-interactive})

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
