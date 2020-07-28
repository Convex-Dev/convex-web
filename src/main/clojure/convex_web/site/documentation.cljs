(ns convex-web.site.documentation
  (:require [convex-web.site.gui :as gui]
            [convex-web.site.backend :as backend]
            [convex-web.site.markdown :as markdown]

            [cljs.spec.alpha :as s]

            ["highlight.js/lib/core" :as hljs]
            ["highlight.js/lib/languages/clojure" :as hljs-clojure]))

(defn ReferencePage [_ {:keys [ajax/status reference] :as state} _]
  (case status
    :ajax.status/pending
    [:div.flex.flex-1.items-center.justify-center
     [gui/Spinner]]

    :ajax.status/error
    [:div.flex.flex-1.items-center.justify-center
     [:span "Sorry. Our servers failed to process your request."]]

    :ajax.status/success
    [:<>
     [:div.flex.flex-col.mt-4.mx-10.overflow-auto
      (for [metadata reference]
        (let [symbol (get-in metadata [:doc :symbol])]
          ^{:key symbol}
          [:div.flex.flex-col.flex-1
           {:id (str "ref-" symbol)
            :ref (fn [el]
                   (when (and el (= symbol (:symbol state)))
                     (.scrollIntoView el)))}
           [gui/SymbolMeta2 metadata]

           [:hr.my-2]]))]

     [:div.inset-y-0.right-0.mr-16.my-6.overflow-auto
      {:style
       {:min-width "220px"}}
      [:div.flex.flex-col.px-10.py-4.bg-yellow-100.rounded.shadow-md
       (for [metadata reference]
         (let [symbol (get-in metadata [:doc :symbol])]
           ^{:key symbol}
           [:a
            {:on-click #(some-> (.getElementById js/document (str "ref-" symbol))
                                (.scrollIntoView))}
            [:code.text-xs.hover:underline.cursor-pointer symbol]]))]]]

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


(defn TutorialPage [_ state _]
  [markdown/Markdown state])

(def tutorial-page
  #:page {:id :page.id/documentation-tutorial
          :title "Tutorial"
          :component #'TutorialPage
          :on-push (markdown/hook-fn :tutorials)})


(defn GettingStartedPage [_ state _]
  [markdown/Markdown state])

(def getting-started-page
  #:page {:id :page.id/documentation-getting-started
          :title "Getting Started"
          :component #'GettingStartedPage
          :on-push (markdown/hook-fn :getting-started)})


(defn ConceptsPage [_ state _]
  [markdown/Markdown state])

(def concepts-page
  #:page {:id :page.id/documentation-concepts
          :title "Concepts"
          :component #'ConceptsPage
          :on-push (markdown/hook-fn :concepts)})

(defn DocumentationPage [_ state _]
  [markdown/Markdown (assoc-in state [:markdown :toc?] false)])

(def documentation-page
  #:page {:id :page.id/documentation
          :title "Documentation"
          :component #'DocumentationPage
          :on-push (markdown/hook-fn :documentation)})