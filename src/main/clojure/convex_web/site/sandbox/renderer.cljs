(ns convex-web.site.sandbox.renderer
  (:require
   [re-frame.core :as rf]
   [lambdaisland.glogi :as log]

   [convex-web.site.command :as command]))

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

(defmethod compile :command
  [{:keys [attributes content]}]
  (let [{attr-source :source} attributes

        ;; Command's content is the first number/string/element.
        [_ content-body] (first content)]

    [:button.bg-blue-500.hover:bg-blue-400.active:bg-blue-600.p-2.rounded.shadow
     {:on-click
      (fn []
        (let [active-address @(rf/subscribe [:session/?active-address])

              command #:convex-web.command {:id (random-uuid)
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
                              (update-in state [:page.id/repl active-address :convex-web.repl/commands] conj response))])))))}

     ;; Command's button text.
     [:span.text-sm.text-white
      (str content-body)]]))

(defmethod compile :h-box
  [ast]
  (let [{:keys [content]} ast]
    (into [:div.flex.flex-row.space-x-2]
      (map
        (fn [[_ ast]]
          (compile ast))
        content))))

(defmethod compile :v-box
  [ast]
  (let [{:keys [content]} ast]
    (into [:div.flex.flex-col.space-y-2]
      (map
        (fn [[_ ast]]
          (compile ast))
        content))))