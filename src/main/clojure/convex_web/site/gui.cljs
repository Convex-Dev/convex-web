(ns convex-web.site.gui
  (:require [convex-web.site.format :as format]

            ["highlight.js/lib/core" :as hljs]
            ["highlight.js/lib/languages/clojure"]
            ["highlight.js/lib/languages/javascript"]

            ["react-tippy" :as tippy]
            ["react-markdown" :as ReactMarkdown]

            ["@tailwindui/react" :as tailwindui-react]

            ["jdenticon" :as jdenticon]

            [reagent.core :as reagent]
            [reitit.frontend.easy :as rfe]))

(defn event-target-value [event]
  (some-> event
          (.-target)
          (.-value)))

(defn scroll-into-view [id]
  (some-> (.getElementById js/document id)
          (.scrollIntoView)))

(defn scroll-element-into-view [el]
  (when el
    (.scrollIntoView el)))

(defn highlight-block [el]
  (when el
    (.highlightBlock hljs el)))

(defn Dismissible
  "Dismiss child component when clicking outside."
  [{:keys [on-dismiss]} child]
  (let [el (reagent/atom nil)

        handler (fn [e]
                  (when-let [el @el]
                    (when-not (.contains el (.-target e))
                      (on-dismiss))))]

    (reagent/create-class
      {:component-did-mount
       (fn [_]
         (.addEventListener js/document "click" handler false))

       :component-will-unmount
       (fn [_]
         (.removeEventListener js/document "click" handler false))

       :reagent-render
       (fn [_]
         [:div
          {:ref
           #(when %
              (reset! el %))}
          child])})))

(defn Transition
  "The Transition component lets you add enter/leave transitions to
   conditionally rendered elements, using CSS classes to control the actual
   transition styles in the different stages of the transition.

   enter: Applied the entire time an element is entering. Usually you define
   your duration and what properties you want to transition here, for example
   transition-opacity duration-75.

   enter-from: The starting point to enter from, for example opacity-0 if
   something should fade in.

   enter-to: The ending point to enter to, for example opacity-100 after fading in.

   leave: Applied the entire time an element is leaving. Usually you define
   your duration and what properties you want to transition here, for example
   transition-opacity duration-75.

   leave-from: The starting point to leave from, for example opacity-100 if something should fade out.

   leave-to: The ending point to leave to, for example opacity-0 after fading
   out.

   https://github.com/tailwindlabs/tailwindui-react#transition"
  [{:keys [show?
           enter
           enter-from
           enter-to
           leave
           leave-from
           leave-to]}
   child]
  [:> tailwindui-react/Transition
   {:show show?
    :enter enter
    :enterFrom enter-from
    :enterTo enter-to
    :leave leave
    :leaveFrom leave-from
    :leaveTo leave-to}

   child])

(def dropdown-transition
  {:enter "transition ease-out duration-100"
   :enter-from "transform opacity-0 scale-95"
   :enter-to "transform opacity-100 scale-100"
   :leave "transition ease-in duration-75"
   :leave-from "transform opacity-100 scale-100"
   :leave-to "transform opacity-0 scale-95"})

(defn ConvexLogo [& [attrs]]
  [:svg
   (merge {:viewBox "0 0 56 64"
           :version "1.1"}
          attrs)
   [:title "Convex logo"]
   [:desc "Created with Sketch."]
   [:g#v0.1 {:stroke "none" :stroke-width "1" :fill "none" :fill-rule "evenodd"}
    [:g#Home {:transform "translate(-692.000000, -139.000000)"}
     [:g#section-hero
      [:g#Hero-Block-Content {:transform "translate(256.000000, 139.000000)"}
       [:g#Convex-logo {:transform "translate(436.000000, 0.000000)"}
        [:polygon#Fill-1 {:fill "#132773" :points "28.0008 64 55.9988 48 55.9988 16.002"}]
        [:polygon#Fill-2 {:fill "#4B74CF" :points "0 47.9998 28 63.9998 0 15.9998"}]
        [:polygon#Fill-3 {:fill "#62A6E1" :points "28.0008 -0.0008 28.0008 63.9992 55.9988 16.0032 55.9988 15.9992"}]
        [:polygon#Fill-4 {:fill "#C3EAFC" :points "28.0008 -0.0008 0.0008 15.9992 28.0008 63.9992"}]]]]]]])

(defn InformationCircleIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"}]])

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

(defn BulletIcon [& [attrs]]
  [:svg
   (merge {:width "40px"
           :height "40px"
           :viewBox "0 0 40 40"
           :version "1.1"}
          attrs)
   [:title "Group"]
   [:g#Symbols {:stroke "none" :stroke-width "1" :fill "none" :fill-rule "evenodd"}
    [:g#Group
     [:rect#Rectangle {:fill "#F3F9FE" :x "0" :y "0" :width "40" :height "40" :rx "4"}]
     [:g#Icon {:transform "translate(4.000000, 4.000000)"}
      [:mask#mask-2 {:fill "white"}
       [:polygon#path-1 {:points "16.3031733 4 5.33333333 10.3333333 5.33333333 23 16.3031733 29.3333333 27.2725067 23 27.2725067 10.3333333"}]]
      [:polygon#path-1 {:fill "#62A6E1" :points "16.3031733 4 5.33333333 10.3333333 5.33333333 23 16.3031733 29.3333333 27.2725067 23 27.2725067 10.3333333"}]]]]])

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

(defn IconChevronDown [& [attrs]]
  [:svg.chevron-down.w-6.h-6
   (merge {:viewBox "0 0 20 20"
           :fill "currentColor"}
          attrs)
   [:path {:fill-rule "evenodd" :d "M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" :clip-rule "evenodd"}]])

(defn Highlight [source & [{:keys [language]}]]
  (let [languages {:convex-lisp "language-clojure"
                   :convex-scrypt "language-javascript"}

        language (get languages language "language-clojure")]
    [:div.shadow.overflow-auto
     [:pre.m-0
      [:code.text-xs.rounded
       {:class language
        :ref highlight-block}
       (str source)]]]))

(defn SymbolType [type]
  [:div.px-1.border.rounded-full
   {:class (case type
             :function
             "bg-blue-100"

             :macro
             "bg-purple-100"

             :special
             "bg-orange-100"

             "bg-gray-100")}
   [:code.text-xs.text-gray-700 type]])

(defn SymbolMetaStrip [sym metadata]
  [:div.flex.justify-between.items-center

   [:div
    {:class "flex items-center justify-between w-1/5"}
    ;; -- Symbol
    [:code.font-bold.text-xs.text-indigo-500.mr-4 sym]

    ;; -- Type
    (when-let [type (get-in metadata [:doc :type])]
      [SymbolType type])]

   [:div
    {:class "flex justify-between w-4/5"}

    ;; -- Description
    (when-let [description (get-in metadata [:doc :description])]
      [:p.text-sm.text-gray-800.ml-10 description])]])

(defn SymbolMeta2 [{:keys [doc show-examples?] :or {show-examples? true}}]
  (let [{:keys [symbol examples type description signature]} doc]
    [:div.flex.flex-col.flex-1.text-sm.p-2
     [:div.flex.items-center

      ;; -- Symbol
      [:code.font-bold.mr-2 symbol]

      ;; -- Type
      (when type
        [SymbolType type])

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
     (when show-examples?
       (when (seq examples)
         [:div.flex.flex-col.items-start.my-2

          [:span.text-sm.text-black.text-opacity-75.mt-2.mb-1 "Examples"]

          (for [{:keys [code]} examples]
            ^{:key code}
            [:pre.text-xs.mb-1
             [:code.clojure.rounded
              {:ref highlight-block}
              code]])]))]))

(defn DefaultButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["text-sm"
              "px-4 py-2"
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


;; Select
;; ===============

(defn SelectOption [{:keys [id value] :as option}]
  (let [missing-id? (nil? id)
        value-not-str? (not (string? value))
        warn? (or missing-id? value-not-str?)
        group-label "SelectOption"

        value (if-not (string? value)
                (str value)
                value)

        id (or id value)]

    (when warn?
      (js/console.group group-label)

      (when missing-id?
        (js/console.warn "`:id` is missing;"))

      (when value-not-str?
        (js/console.warn "`:value` should be an string;"))

      (js/console.warn "In:" (prn-str option))

      (js/console.groupEnd group-label))

    [:option {:value id} value]))

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
     ^{:key option} [SelectOption {:value option}])])

(defn Select2
  "- `selected` is the `:id` value of option;

   - `options` is a collection of maps with keys `:id` and `:value`;

   - `on-change` will be called with the selected `:id`;"
  [{:keys [selected options on-change]}]
  [:select
   {:class
    ["text-sm"
     "p-1"
     "rounded"
     "focus:outline-none"
     "bg-gray-100 hover:shadow-md"]

    ;; `:value` is the React way to select a value.
    ;; Reference: https://reactjs.org/docs/forms.html#the-select-tag
    :value (or selected "")

    :on-change
    (fn [event]
      (let [id-value (.-value (.-target event))

            selected-id (some
                          (fn [{:keys [id]}]
                            (let [id-converted (if (or (keyword? id) (symbol? id))
                                                 (name id)
                                                 id)]
                              (when (= id-converted id-value)
                                id)))
                          options)]
        (on-change selected-id)))}
   (for [option options]
     ^{:key (:id option)} [SelectOption option])])

;; ----------------------------------------------------

(defn SpinnerSmall []
  [:div.spinner.ease-linear.rounded-full.border-2.border-t-2.border-gray-200.h-4.w-4])

(defn Spinner []
  [:div.spinner.ease-linear.rounded-full.border-4.border-t-4.border-gray-200.h-10.w-10])

(defn ClipboardCopy [text & [{:keys [color background-color hover margin]}]]
  (let [id (str (random-uuid))]
    [:div
     (merge {} (when margin {:class margin}))
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
  (let [{:convex-web.account-status/keys [memory-size
                                          allowance
                                          balance
                                          sequence
                                          environment
                                          type]} status

        address-blob (format/address-blob address)]
    [:div.flex.flex-col.justify-center.items-center

     [:div.flex.flex-col.items-center.w-full.leading-none
      [:span.text-xs.text-gray-700.uppercase "Balance"]
      [:span.mt-1.text-4xl (format/format-number (str (or balance "")))]]

     [:div.flex.flex-col.items-center.w-full.leading-none.mt-10
      [:span.text-xs.text-gray-700.uppercase "Address"]
      [:div.flex.items-center.mt-1
       [:code.text-sm.mr-2 address-blob]
       [ClipboardCopy address-blob]]]

     [:div.flex.flex-col.items-center.w-full.leading-none.mt-10
      [:span.text-xs.text-gray-700.uppercase "Memory Allowance"]
      [:code.mt-1.text-sm allowance]]

     [:div.flex.flex-col.items-center.w-full.leading-none.mt-10
      [:span.text-xs.text-gray-700.uppercase "Memory Size"]
      [:code.mt-1.text-sm memory-size]]

     [:div.flex.flex-col.items-center.w-full.leading-none.mt-10
      [:span.text-xs.text-gray-700.uppercase "Sequence"]
      [:code.mt-1.text-sm sequence]]

     [:div.flex.flex-col.items-center.w-full.leading-none.mt-10
      [:span.text-xs.text-gray-700.uppercase "Type"]
      [:code.mt-1.text-sm type]]

     [:span.text-xs.text-gray-700.uppercase.mt-10 "Environment"]
     [:div.flex.flex-col.items-center.w-full.px-10.overflow-auto.border.rounded.p-2.mt-1.bg-gray-100
      [:div.flex.flex-col.w-full.divide-y
       (let [environment (sort-by (comp str first) environment)]
         (if (seq environment)
           (for [[sym metadata] environment]
             ^{:key sym}
             [:div.w-full.py-2.px-1.hover:bg-gray-100.rounded
              [SymbolMetaStrip sym (:convex-web.syntax/meta metadata)]])
           [:span.text-xs.text-gray-700.text-center "Empty"]))]]]))

(defn RangeNavigation [{:keys [start end total on-previous-click on-next-click]}]
  [:div.flex.py-2

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

(defn Identicon
  "Reagent wrapper for Jidenticon.

   - value is considered a hash string if the string is hexadecimal and
   contains at least 11 characters. It is otherwise considered a value that
   will be hashed using SHA1.

   - size defines the width and height, icons are always square, of the icon
   in pixels, including its padding.

   https://jdenticon.com"
  [{:keys [value size]}]
  [:div
   {:ref (fn [el]
           (when el
             (set! (.-innerHTML el) (jdenticon/toSvg value size))))}])