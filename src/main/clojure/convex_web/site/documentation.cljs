(ns convex-web.site.documentation
  (:require [convex-web.site.gui :as gui]
            [convex-web.site.backend :as backend]
            [convex-web.site.markdown :as markdown]

            [clojure.string :as str]
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
    [:div.flex.flex-1.overflow-auto

     ;; Documentation
     [:div.flex.flex-col.overflow-auto {:class "w-1/2"}
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

     ;; TOC
     [:div.ml-16.mb-16.bg-yellow-100.rounded.shadow.overflow-auto
      {:class "w-1/2"
       :style {:min-width "220px"}}
      [:div.flex.flex-col.leading-snug.p-4
       (let [reference-grouped-by (->> reference
                                       (group-by
                                         (fn [metadata]
                                           (let [[c] (get-in metadata [:doc :symbol])]
                                             (if (re-matches #"^\w" c)
                                               (str/upper-case c)
                                               "*"))))
                                       (sort-by first))]

         (for [[k metas] reference-grouped-by]
           ^{:key k}
           [:div.flex.flex-wrap.items-end.mb-1
            [:code.text-blue-400.text-xs.font-bold.mr-2 k]
            (for [meta metas]
              (let [symbol (get-in meta [:doc :symbol])]
                ^{:key symbol}
                [:a.underline.mr-2
                 {:on-click #(some-> (.getElementById js/document (str "ref-" symbol))
                                     (.scrollIntoView))}
                 [:code.text-xs.hover:underline.cursor-pointer symbol]]))]))]]]

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
          :on-push (markdown/get-on-push :tutorials)})


(defn GettingStartedPage [_ state _]
  [markdown/Markdown state])

(def getting-started-page
  #:page {:id :page.id/documentation-getting-started
          :title "Getting Started"
          :component #'GettingStartedPage
          :on-push (markdown/get-on-push :under-construction)})


(defn DocumentationPage [_ state _]
  [markdown/Markdown state])

(def documentation-page
  #:page {:id :page.id/documentation
          :title "Documentation"
          :component #'DocumentationPage
          :on-push (markdown/get-on-push :documentation)})


(defn AboutPage [_ state _]
  [markdown/Markdown state])

(def about-page
  #:page {:id :page.id/about
          :title "About"
          :component #'AboutPage
          :on-push (markdown/get-on-push :under-construction)})

(defn FaqPage [_ state _]
  [markdown/Markdown state])

(def faq-page
  #:page {:id :page.id/faq
          :title "FAQ"
          :component #'FaqPage
          :on-push (markdown/get-on-push :faq)})


(defn ConceptsPage [_ state _]
  [markdown/Markdown state])

(def concepts-page
  #:page {:id :page.id/concepts
          :title "Concepts"
          :component #'ConceptsPage
          :on-push (markdown/get-on-push :under-construction)})


(defn WhitePaperPage [_ state _]
  [markdown/Markdown state])

(def white-paper-page
  #:page {:id :page.id/white-paper
          :title "White Paper"
          :component #'WhitePaperPage
          :on-push (markdown/get-on-push :white-paper)})

(defn VisionPage [_ state _]
  [markdown/Markdown state])

(def vision-page
  #:page {:id :page.id/vision
          :title "Vision"
          :component #'VisionPage
          :on-push (markdown/get-on-push :vision)})

(defn AdvancedTopicsPage [_ state _]
  [markdown/Markdown state])

(def advanced-topics-page
  #:page {:id :page.id/advanced-topics
          :title "Advanced Topics"
          :component #'AdvancedTopicsPage
          :on-push (markdown/get-on-push :advanced-topics)})

(defn UnderConstructionPage [_ state _]
  [markdown/Markdown state])

(def under-construction-page
  #:page {:id :page.id/under-construction
          :title "Under Construction"
          :component #'UnderConstructionPage
          :on-push (markdown/get-on-push :under-construction)})