(ns convex-web.site.sandbox.renderer
  "Reagent renderer for Interactive Sandbox.

  Compiles an AST produced by spec/conform to a Reagent component.

  Currently implemented tags:
    - :text
    - :command
    - :h-box
    - :v-box

  A tag is implemented as a multimethod,
  so it's possible to extend the language via new methods/tags."
  (:require
   [re-frame.core :as rf]
   [lambdaisland.glogi :as log]
   [cljfmt.core :as cljfmt]

   [convex-web.site.command :as command]

   ["highlight.js/lib/languages/clojure"]
   ["highlight.js/lib/languages/javascript"]
   ["react-highlight.js" :as react-hljs]
   ["@heroicons/react/solid" :as icon]))

(defmulti compile :tag)

(defmethod compile :default
  [ast]
  [:code (str ast)])

(defmethod compile :text
  [{:keys [content]}]
  ;; Text's content is the first number/string/element.
  (let [[_ content-body] (first content)]
    [:span
     (str content-body)]))

(defmethod compile :code
  [{:keys [content]}]
  ;; Code's content is the first number/string/element.
  (let [[_ content-body] (first content)]
    [:div.text-xs.overflow-auto
     [:> (.-default react-hljs) {:language "language-clojure"}
      (try
        (cljfmt/reformat-string (str content-body))
        (catch js/Error _
          (str content-body)))]]))

(defmethod compile :button
  [{:keys [attributes content]}]
  (let [{attr-source :source
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
            (log/warn :unknown-action attr-action))))}

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
  [ast]
  (let [{:keys [content]} ast]
    (into [:div.flex.flex-row.items-center.space-x-3]
      (map
        (fn [[_ ast]]
          (compile ast))
        content))))

(defmethod compile :v-box
  [ast]
  (let [{:keys [content]} ast]
    (into [:div.flex.flex-col.items-start.space-y-3]
      (map
        (fn [[_ ast]]
          (compile ast))
        content))))