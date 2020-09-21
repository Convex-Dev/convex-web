(ns convex-web.site.environment
  (:require [convex-web.site.gui :as gui]))

(defn EntryPage [_ {:keys [symbol syntax]} _]
  (let [{:convex-web.syntax/keys [meta value]} syntax

        caption-style "text-gray-600 text-base"
        caption-container-style "flex flex-col space-y-1 leading-none"]
    [:div.flex.flex-col.flex-1.items-start.space-y-4.p-4
     ;; -- Symbol
     [:div {:class caption-container-style}
      [:span {:class caption-style} "Symbol"]
      [:div.flex.items-center.space-x-2
       [gui/Highlight symbol]
       [gui/ClipboardCopy symbol]]]

     ;; -- Value
     [:div {:class caption-container-style}
      [:span {:class caption-style} "Value"]
      [:div.flex.items-center..space-x-2
       [gui/Highlight value]
       [gui/ClipboardCopy value]]]]))

(def entry-page
  #:page {:id :page.id/environment-entry
          :title "Environment Entry"
          :component #'EntryPage})
