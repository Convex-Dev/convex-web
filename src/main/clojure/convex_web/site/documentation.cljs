(ns convex-web.site.documentation
  (:require [convex-web.site.gui :as gui]
            [convex-web.site.backend :as backend]
            [convex-web.convex :as convex]
            
            [clojure.string :as str]
            [cljs.spec.alpha :as s]))

(defn ReferencePage [_ {:keys [ajax/status selected-library reference] :as state} set-state]
  (case status
    :ajax.status/pending
    [:div.flex.flex-1.items-center.justify-center
     [gui/Spinner]]
    
    :ajax.status/error
    [:div.flex.flex-1.items-center.justify-center
     [:span "Sorry. Our servers failed to process your request."]]
    
    :ajax.status/success
    (let [libraries (sort (keys reference))
          
          ;; Remove private and sort by symbol.
          selected-library-reference (->> (get reference selected-library)
                                       (remove
                                         (fn [[_ {:keys [private?]}]]
                                           private?))
                                       (sort-by first))]
      [:div.flex.flex-col.flex-1.space-y-3.overflow-auto
       
       ;; Libraries select.
       [:div.flex.items-center.space-x-3
        [:span.font-mono.text-sm.text-gray-500
         "Library"]
        
        [gui/Select2
         {:selected selected-library
          :options
          (map 
            (fn [library-name]
              {:id library-name
               :value library-name})
            libraries)
          :on-change #(set-state assoc :selected-library %)}]]
       
       ;; Content.
       [:div.flex.flex-1.overflow-auto
        
        ;; Documentation
        [:div.flex.flex-col.overflow-auto {:class "md:w-1/2"}
         
         [:p.prose.prose-sm.mb-5
          (convex/library-documentation selected-library "")]
         
         (for [[symbol metadata] selected-library-reference]
           ^{:key symbol}
           [:div.flex.flex-col.flex-1
            {:id (str "ref-" symbol)
             :ref (fn [el]
                    (when (and el (= (name symbol) (:symbol state)))
                      (.scrollIntoView el)))}
            [gui/SymbolMeta
             {:library selected-library
              :symbol symbol
              :metadata metadata
              :show-examples? true}]
            
            [:hr.my-2]])]
        
        ;; TOC
        [:div.hidden.md:block.ml-16.mb-6.bg-yellow-100.rounded.shadow.overflow-auto
         {:class "w-1/2"
          :style {:min-width "220px"}}
         [:div.flex.flex-col.leading-snug.p-4
          (let [reference-grouped-by (->> selected-library-reference
                                       (group-by
                                         (fn [[sym _]]
                                           (let [[c] (name sym)]
                                             (if (re-matches #"^\w" c)
                                               (str/upper-case c)
                                               "*"))))
                                       (sort-by first))]
            
            (for [[k reference] reference-grouped-by]
              ^{:key k}
              [:div.flex.flex-wrap.mb-1
               [:div 
                [:code.text-blue-500.text-xs.font-bold.mr-2 k]]
               (for [[symbol _] reference]
                 ^{:key symbol}
                 [:a.mr-2
                  {:on-click #(some-> (.getElementById js/document (str "ref-" symbol)) (.scrollIntoView))}
                  [:code.text-xs.hover:underline.cursor-pointer symbol]])]))]]]])
    
    [:div]))

(s/def :reference-page.state/symbol :convex-web/non-empty-string)
(s/def :reference-page.state/reference coll?)

(def reference-page
  #:page {:id :page.id/documentation-reference
          :title "Reference"
          :component #'ReferencePage
          :state-spec
          (s/nilable
            (s/keys :opt-un [:reference-page.state/symbol
                             :reference-page.state/reference]))
          :initial-state {:selected-library "convex.core"}
          :on-push
          (fn [_ _ set-state]
            (set-state assoc :ajax/status :ajax.status/pending)

            (backend/GET-reference
              {:handler
               (fn [reference]
                 (set-state assoc
                            :ajax/status :ajax.status/success
                            :reference reference))

               :error-handler
               (fn [error]
                 (set-state assoc
                            :ajax/status :ajax.status/error
                            :ajax/error error))}))})

