(ns convex-web.site.gui
  (:require [convex-web.site.format :as format]

            ["highlight.js/lib/core" :as hljs]
            ["react-tippy" :as tippy]
            ["react-markdown" :as ReactMarkdown]

            [reagent.core :as reagent]
            [reitit.frontend.easy :as rfe]))

(defn event-target-value [event]
  (some-> event
          (.-target)
          (.-value)))

(defn scroll-into-view [id]
  (some-> (.getElementById js/document id)
          (.scrollIntoView)))

(defn highlight-block [el]
  (when el
    (.highlightBlock hljs el)))

(defn PlayIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"}] [:path {:d "M21 12a9 9 0 11-18 0 9 9 0 0118 0z"}]])

(defn TagIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"}]])

(defn SortAscendingIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M3 4h13M3 8h9m-9 4h6m4 0l4-4m0 0l4 4m-4-4v12"}]])

(defn SortDescendingIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M3 4h13M3 8h9m-9 4h9m5-4v12m0 0l-4-4m4 4l4-4"}]])

(defn CopyClipboardIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3"}]])

(defn CheckIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M5 13l4 4L19 7"}]])

(defn IconXCircle [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"}]])

(defn IconAdjustments [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :stroke "currentColor"
           :viewBox "0 0 24 24"}
          attrs)
   [:path {:d "M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"}]])

(defn IconExternalLink [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :stroke "currentColor"
           :viewBox "0 0 24 24"}
          attrs)
   [:path {:d "M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"}]])

(defn IconEye [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M15 12a3 3 0 11-6 0 3 3 0 016 0z"}]
   [:path {:d "M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"}]])

(defn IconUser [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :stroke "currentColor"
           :viewBox "0 0 24 24"}
          attrs)
   [:path {:d "M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"}]])

(defn IconButtonCode [{:keys [color hover-background on-click] :or {on-click identity}}]
  [:a.rounded-full.h-8.w-8.flex.items-center.justify-center.text-xl.tooltip
   {:class (or hover-background "hover:bg-gray-300")
    :aria-label "Copy to editor"
    :data-balloon-pos "up"
    :on-click on-click}
   [:i.zmdi.zmdi-code
    {:class (or color "text-gray-700")}]])

(defn Highlight [source]
  [:div.shadow.overflow-auto
   [:pre.m-0
    [:code.text-xs.rounded.language-clojure
     {:ref highlight-block}
     source]]])

(defn SymbolMeta [sym metadata]
  [:div.flex.flex-col
   [:code.font-bold.text-xs.text-indigo-500.mb-2 sym]

   ;; -- Signature
   (when-let [signature (get-in metadata [:doc :signature])]
     [:div.flex.flex-col
      [:span.text-xs.font-bold.mb-1 "Signature"]

      (for [{:keys [params]} signature]
        (let [params (str (vec params))]
          ^{:key params} [:p.text-sm.m-0.mb-1 params]))])

   ;; -- Description
   (when-let [description (get-in metadata [:doc :description])]
     [:p.text-sm.mt-1 description])

   ;; -- Examples
   (when-let [examples (get-in metadata [:doc :examples])]
     [:div.flex.flex-col.mt-2
      [:span.text-xs.font-bold "Examples"]

      (for [{:keys [code]} examples]
        (let [code (str code)]
          ^{:key code} [:p.text-sm.mt-1 code]))])])

(defn SymbolMeta2 [{:keys [doc]}]
  (let [{:keys [symbol examples type description signature]} doc]
    [:div.flex.flex-col.flex-1.text-sm.p-2
     [:div.flex.items-center

      ;; -- Symbol
      [:code.font-bold symbol]

      ;; -- Type
      (when type
        [:div.ml-2.px-1.border.rounded-full
         {:class (case type
                   :function
                   "bg-blue-100"

                   :macro
                   "bg-purple-100"

                   :special
                   "bg-orange-100"

                   "bg-gray-100")}
         [:code.text-xs.text-gray-700 type]])

      [:a.ml-2
       {:href (rfe/href :route-name/documentation-reference {} {:symbol symbol})
        :target "_blank"}
       [IconExternalLink {:class "h-4 w-4 text-gray-500 hover:text-black"}]]]

     ;; -- Signature
     [:div.flex.flex-col.my-2
      (for [{:keys [params]} signature]
        ^{:key params}
        [:code.text-xs (str params)])]

     ;; -- Description
     [:span.my-2 description]

     ;; -- Examples
     (when (seq examples)
       [:div.flex.flex-col.items-start.my-2

        [:span.text-sm.text-black.text-opacity-75.mt-2.mb-1 "Examples"]

        (for [{:keys [code]} examples]
          ^{:key symbol}
          [:pre.text-xs.mb-1
           [:code.clojure.rounded
            {:ref highlight-block}
            code]])])]))

(defn DefaultButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["text-sm"
              "px-2 py-1"
              "bg-gray-100"
              "rounded"
              "focus:outline-none"
              "hover:shadow-md"
              (if disabled?
                "text-gray-500 pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn Tooltip
  "Reagent wrapper for React Tippy.

   https://github.com/tvkhoa/react-tippy#react-tippy"
  [attrs child]
  (let [attrs (if (string? attrs)
                {:title attrs}
                attrs)]
    [:> tippy/Tooltip attrs child]))

(defn Select [{:keys [value options on-change]}]
  [:select
   {:class
    ["text-sm"
     "p-1"
     "rounded"
     "focus:outline-none"
     "bg-gray-100 hover:shadow-md"]
    :value (or value "")
    :on-change (fn [event]
                 (on-change (.-value (.-target event))))}
   (for [option options]
     ^{:key option} [:option {:value option} option])])

(defn SpinnerSmall []
  [:div.spinner.ease-linear.rounded-full.border-2.border-t-2.border-gray-200.h-4.w-4])

(defn Spinner []
  [:div.spinner.ease-linear.rounded-full.border-4.border-t-4.border-gray-200.h-10.w-10])

(defn ClipboardCopy [text & [{:keys [color background-color hover]}]]
  (let [id (str (random-uuid))]
    [:div
     [:input.absolute
      {:id id
       :type "text"
       :value text
       :aria-hidden true
       :style {:left "-999em"}
       :on-change identity}]

     [Tooltip
      {:title "Copy"}
      [CopyClipboardIcon
       {:class
        ["w-4 h-4 cursor-pointer"
         (or color "text-gray-500")
         (or background-color "")
         (or (some->> hover (apply str)) "hover:text-black")]
        :on-click
        (fn []
          (.select (.getElementById js/document id))
          (.execCommand js/document "copy"))}]]]))

(defn Account [{:convex-web.account/keys [address status]}]
  (let [{:convex-web.account-status/keys [balance sequence environment type]} status

        address-blob (format/address-blob address)]
    [:div.flex.flex-col.justify-center

     [:div.flex.flex-col.items-center.w-full.leading-none
      [:span.text-xs.text-gray-700.uppercase "Balance"]
      [:span.mt-1.text-4xl (format/format-number (str (or balance "")))]]

     [:div.flex.flex-col.items-center.w-full.leading-none.mt-10
      [:span.text-xs.text-gray-700.uppercase "Address"]
      [:div.flex.items-center.mt-1
       [:code.text-sm.mr-2 address-blob]
       [ClipboardCopy address-blob]]]

     [:div.flex.flex-col.items-center.w-full.leading-none.mt-10
      [:span.text-xs.text-gray-700.uppercase "Sequence"]
      [:code.mt-1.text-sm sequence]]

     [:div.flex.flex-col.items-center.w-full.leading-none.mt-10
      [:span.text-xs.text-gray-700.uppercase "Type"]
      [:code.mt-1.text-sm type]]

     [:div.flex.flex-col.items-center.w-full.leading-none.mt-10
      [:span.text-xs.text-gray-700.uppercase "Environment"]
      (for [[sym metadata] environment]
        ^{:key sym}
        [:div.w-full.mb-2.b.border-b
         [SymbolMeta sym metadata]])]]))

(defn RangeNavigation [{:keys [start end total on-previous-click on-next-click]}]
  [:div.flex.p-2

   ;; -- Previous
   [DefaultButton
    {:disabled (= start 0)
     :on-click on-previous-click}
    [:span.text-xs "Previous"]]

   [:div.mx-1]

   ;; -- Next
   [DefaultButton
    {:disabled (= end total)
     :on-click on-next-click}
    [:span.text-xs.select-none "Next"]]


   ;; -- Range
   [:div.flex.ml-10.items-center.border-b
    [:span.text-xs.text-gray-600.uppercase "Start"]
    [:span.text-xs.font-bold.text-indigo-500.ml-1 start]

    [:span.text-xs.text-gray-600.uppercase.ml-2 "End"]
    [:span.text-xs.font-bold.text-indigo-500.ml-1 end]

    [:span.text-xs.text-gray-600.uppercase.ml-4 "Total"]
    [:span.text-xs.font-bold.text-indigo-500.ml-1 total]]])

(defn MarkdownCodeBlock [{:keys [value]}]
  [:pre.relative
   [:div.absolute.right-0.top-0.m-2
    [ClipboardCopy value {:color "text-white"
                          :hover "hover:opacity-75"}]]

   [:code.hljs.language-clojure.text-sm.rounded
    {:ref highlight-block}
    value]])

(defn Markdown [markdown]
  [:> ReactMarkdown
   {:source markdown
    :renderers
    {:code (reagent/reactify-component MarkdownCodeBlock)}}])