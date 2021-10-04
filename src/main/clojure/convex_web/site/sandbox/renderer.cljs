(ns convex-web.site.sandbox.renderer)

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
  [{:keys [content]}]
  ;; Command's content is the first number/string/element.
  (let [[_ content-body] (first content)]
    [:button.bg-blue-500.hover:bg-blue-400.active:bg-blue-600.p-2.rounded.shadow
     {:on-click
      (fn [])}
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