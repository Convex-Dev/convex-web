(ns convex-web.site.gui
  (:require
   [clojure.string :as str]

   [goog.string :as gstring]
   [goog.string.format]
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]

   ["react" :as react]
   ["highlight.js/lib/core" :as hljs]
   ["highlight.js/lib/languages/clojure"]
   ["highlight.js/lib/languages/javascript"]
   ["react-highlight.js" :as react-hljs]
   ["react-tippy" :as tippy]
   ["react-markdown$default" :as ReactMarkdown]
   ["@headlessui/react" :as headlessui]
   ["@heroicons/react/solid" :as icon :refer [XIcon]]
   ["jdenticon" :as jdenticon]))

(defn event-target-value [^js event]
  (some-> event
          (.-target)
          (.-value)))

(defn scroll-into-view
  "https://developer.mozilla.org/en-US/docs/Web/API/Element/scrollIntoView"
  [id & [options]]
  (some-> (.getElementById js/document id)
          (.scrollIntoView (or (clj->js options) #js {}))))

(defn scroll-element-into-view
  "https://developer.mozilla.org/en-US/docs/Web/API/Element/scrollIntoView"
  [el & [options]]
  (when el
    (.scrollIntoView el (or (clj->js options) #js {}))))

(defn highlight-element [el]
  (when el
    (.highlightElement hljs el)))

(defn transaction-type-text-color [transaction-type]
  (case transaction-type
    :convex-web.transaction.type/transfer
    "text-indigo-500"

    :convex-web.transaction.type/invoke
    "text-pink-500"

    ""))

(defn transaction-type-description [transaction-type]
  (case transaction-type
    :convex-web.transaction.type/transfer
    "Direct transfer of Convex Coins from the Signer's Account to a destination Account"

    :convex-web.transaction.type/invoke
    "Execution of code by Signer Account"

    ""))

(defn account-type-text-color [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "text-purple-500"

    (get account-status :convex-web.account-status/actor?)
    "text-indigo-500"

    :else
    "text-green-500"))

(defn account-type-bg-color [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "bg-purple-500"

    (get account-status :convex-web.account-status/actor?)
    "bg-indigo-500"

    :else
    "bg-green-500"))

(defn account-type-label [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "library"

    (get account-status :convex-web.account-status/actor?)
    "actor"

    :else
    "user"))

(defn account-type-description [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "An immutable Account containing code and other static information. A
     Library is essentially an Actor with no exported functionality."

    (get account-status :convex-web.account-status/actor?)
    "An Autonomous Actor on the Convex network, which can be used to implement
     smart contracts."

    :else
    "An external user of Convex."))

(defn merge-account-env
  "Merge account's env with session (lazy) env."
  [{:keys [convex-web/account convex-web.session/state]}]
  (let [{:convex-web.account/keys [address]} account

        ;; Construct env for success only.
        env (reduce-kv
              (fn [acc sym {:keys [value ajax/status]}]
                (when (= status :ajax.status/success)
                  (assoc acc sym value)))
              {}
              (get-in state [address :env]))]

    (update-in account [:convex-web.account/status :convex-web.account-status/environment] merge env)))

(def identicon-size-small 26)
(def identicon-size-large 40)

(defn Jdenticon
  "Reagent wrapper for Jdenticon.

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

(defn AIdenticon
  "Convex Account Identicon."
  [{:keys [value size]}]
  [Jdenticon {:value value
              :size size}])

(defn Dismissible
  "Dismiss child component when clicking outside."
  [{:keys [on-dismiss]} child]
  (let [el (r/atom nil)

        handler (fn [e]
                  (when-let [el @el]
                    (when-not (.contains el (.-target e))
                      (on-dismiss))))]

    (r/create-class
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

   https://github.com/tailwindlabs/headlessui/blob/develop/packages/%40headlessui-react/README.md#transition"
  [{:keys [show?
           enter
           enter-from
           enter-to
           leave
           leave-from
           leave-to]}
   child]
  [:> headlessui/Transition
   {:show show?
    :enter enter
    :enterFrom enter-from
    :enterTo enter-to
    :leave leave
    :leaveFrom leave-from
    :leaveTo leave-to}

   child])

(defn SlideOver [{:keys [open? on-close]} child]
  ;; Coerce to boolean because nil is invalid.
  (let [open? (boolean open?)]
    [:> headlessui/Transition.Root
     {:show open?
      :as react/Fragment}

     [:> headlessui/Dialog
      {:as "div"
       :static true
       :className "z-50 fixed inset-0 overflow-hidden"
       :open open?
       :onClose (or on-close identity)}

      (r/as-element
        [:div.absolute.inset-0.overflow-hidden

         [:> headlessui/Dialog.Overlay
          {:className "fixed inset-0"}]

         [:div.fixed.inset-y-0.right-0.pl-10.max-w-full.flex.sm:pl-16

          [:> headlessui/Transition.Child
           {:as react/Fragment
            :enter "transform transition ease-in-out duration-500"
            :enterFrom "translate-x-full"
            :enterTo "translate-x-0"
            :leave "transform transition ease-in-out duration-500"
            :leaveFrom "translate-x-0"
            :leaveTo "translate-x-full"}

           [:div.w-screen.max-w-lg
            [:div.h-full.flex.flex-col.py-6.bg-white.shadow-xl.overflow-y-scroll

             ;; Header.
             [:div.px-4.sm:px-6
              [:div.h-7.flex.justify-end
               [:button
                {:className "bg-white rounded-md text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                 :onClick on-close}
                [:span {:className "sr-only"} "Close panel"]
                [:> XIcon {:className "h-6 w-6" :aria-hidden "true"}]]]]

             ;; Body.
             [:div.mt-6.relative.flex-1.px-4.sm:px-6
              [:div.absolute.inset-0.px-4.sm:px-6
               [:div.h-full
                {:aria-hidden "true"}
                child]]]

             ]]]]])]]))

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

(defn Convex
  "Convex logo and text."
  []
  [:a {:href (rfe/href :route-name/welcome)}
   [:div.flex.items-center.space-x-4
    
    [ConvexLogo {:width "28px" :height "32px"}]
    
    [:span.text-base.md:text-2xl.font-bold.leading-none.text-blue-800 "CONVEX"]]])

(defn ConvexLogoWhiteLink []
  [:a {:href (rfe/href :route-name/welcome)}
   [:img
    {:src "/images/convex_logo_white.svg"}]])

(defn ConvexWhite
  "Convex logo and text."
  []
  [:a {:href (rfe/href :route-name/welcome)}
   [:div.flex.items-center.space-x-4
    
    [ConvexLogo {:width "28px" :height "32px"}]
    
    [:span.text-base.md:text-2xl.font-bold.leading-none.text-white "CONVEX"]]])

(defn ArrowCircleRightIcon [& [attrs]]
  [:svg.w-6.h-6
   (merge {:fill "none"
           :stroke "currentColor"
           :viewBox "0 0 24 24"
           :xmlns "http://www.w3.org/2000/svg"}
          attrs)
   [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M13 9l3 3m0 0l-3 3m3-3H8m13 0a9 9 0 11-18 0 9 9 0 0118 0z"}]])

(defn ArrowCircleDownIcon [& [attrs]]
  [:svg.w-6.h-6
   (merge {:fill "none"
           :stroke "currentColor"
           :viewBox "0 0 24 24"
           :xmlns "http://www.w3.org/2000/svg"}
          attrs)
   [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M15 13l-3 3m0 0l-3-3m3 3V8m0 13a9 9 0 110-18 9 9 0 010 18z"}]])

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

(defn PlusIcon [& [attrs]]
  [:svg.w-6.h-6
   (merge {:fill "none"
           :stroke "currentColor"
           :viewBox "0 0 24 24"
           :xmlns "http://www.w3.org/2000/svg"}
          attrs)
   [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M12 6v6m0 0v6m0-6h6m-6 0H6"}]])

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

(defn MenuAlt3Icon [& [attrs]]
  [:svg
   (merge {:viewBox "0 0 20 20"
           :fill "currentColor"}
          attrs)
   [:path
    {:fill-rule "evenodd" :d "M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h6a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" :clip-rule "evenodd"}]])

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

(defn IconChevronUp [& [attrs]]
  [:svg
   (merge {:viewBox "0 0 20 20"
           :fill "currentColor"}
          attrs)
   [:path {:fillRule "evenodd" :d "M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z" :clipRule "evenodd"}]])

(defn GitHubIcon [& [attrs]]
  [:svg
   (merge {:xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :viewBox "0 0 24 24"}
          attrs)
   [:path {:d "M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"}]])

(defn RefreshIcon [& [attrs]]
  [:svg
   (merge {:viewBox "0 0 20 20"
           :fill "currentColor"}
          attrs)
   [:path {:fillRule "evenodd" :d "M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" :clipRule "evenodd"}]])

(defn QuestionMarkCircle [& [attrs]]
  [:svg
   (merge {:fill "none" :stroke "currentColor" :viewBox "0 0 24 24" :xmlns "http://www.w3.org/2000/svg"} attrs)
   [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"}]])

(defn Highlight [source & [{:keys [language]}]]
  (let [languages {:convex-lisp "language-clojure"}
        language (get languages language "language-clojure")]
    [:div.text-xs.overflow-auto
     [:> (.-default react-hljs) {:language language}
      source]]))

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

(defn SymbolMeta [{:keys [library symbol metadata show-examples?]}]
  (let [{:keys [examples type description signature]} (:doc metadata)]
    [:div.flex.flex-col.flex-1.text-sm.p-2.space-y-3
     [:div.flex.items-center
      
      ;; -- Symbol
      [:code.font-bold.mr-2 symbol]
      
      ;; -- Type
      (when type
        [SymbolType type])
      
      [:a.ml-2
       {:href (rfe/href :route-name/cvm.reference {}
                (merge {:symbol symbol}
                  ;; Router defaults to convex.core
                  ;; if there isn't a library parameter.
                  (when library
                    {:library library})))
        :target "_blank"}
       [IconExternalLink {:class "h-4 w-4 text-gray-500 hover:text-black"}]]]
     
     ;; -- Signature
     [:div.flex.flex-col
      (for [{:keys [params]} signature]
        ^{:key params}
        [:code.text-xs (str params)])]
     
     ;; -- Description
     ;; (It can be either a string or a collection of string)
     (if (string? description)
       [:p description]
       [:div.flex.flex-col.space-y-2 
        (for [s description]
          ^{:key s}
          [:p s])])
     
     ;; -- Examples
     (when show-examples?
       (when (seq examples)
         [:div.flex.flex-col.items-start.my-2
          
          [:span.text-sm.text-black.text-opacity-75.mt-2.mb-1 "Examples"]
          
          (for [{:keys [code]} examples]
            ^{:key code}
            [:div.w-full.overflow-auto
             [:pre.text-xs.mb-1
              [:code.clojure.rounded
               {:ref highlight-element}
               code]]])]))]))

(def hyperlink-hover-class "hover:underline hover:text-blue-500")

(def button-child-small-padding "px-3 md:px-6 py-2")
(def button-child-large-padding "px-8 py-3")

(defn ButtonText [{:keys [padding text-size text-color text-transform font-family]} text]
  [:span.block
   {:class [(or padding button-child-large-padding)
            (or text-size "text-sm")
            (or text-color "text-white")
            (or text-transform "uppercase")
            (or font-family "font-mono")]}
   text])

(defn Caption [text]
  [:span.text-gray-600.text-base.leading-none.cursor-default text])

(defn CaptionMono [text]
  [:span.font-mono.text-gray-600.text-base.leading-none.cursor-default text])

(def input-style "h-10 text-sm border rounded-md px-4 bg-blue-100 bg-opacity-25")

(defn DefaultButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["text-sm"
              "px-2.5 py-1.5"
              "bg-gray-100"
              "rounded"
              "shadow-md"
              "focus:outline-none"
              "hover:opacity-75"
              "active:bg-gray-200"
              (when disabled?
                "text-gray-500 pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn BlackButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["px-4 py-3"
              "bg-gray-900 hover:bg-gray-700 active:bg-black"
              "rounded"
              "shadow-md"
              "focus:outline-none"
              (when disabled?
                "pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn TealButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["px-4 py-3"
              "bg-teal-500 hover:bg-teal-400 active:bg-teal-700"
              "rounded"
              "shadow-md"
              "focus:outline-none"
              (when disabled?
                "pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn BlueButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["px-4 py-3"
              "bg-blue-500 hover:bg-blue-400 active:bg-blue-700"
              "border-2 border-blue-500 rounded"
              "shadow-md"
              "focus:outline-none"
              (when disabled?
                "pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn BlueOutlineButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["px-4 py-3"
              "hover:bg-blue-400 active:bg-blue-700"
              "border-2 border-blue-500 rounded"
              "shadow-md"
              "focus:outline-none"
              (when disabled?
                "pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn PrimaryButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["rounded"
              "shadow-md"
              "focus:outline-none"
              (if disabled?
                "pointer-events-none bg-blue-300"
                "bg-blue-500 hover:bg-blue-400 active:bg-blue-600")]
             :on-click identity}
            attrs)
     child]))

(defn SecondaryButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["bg-indigo-500 hover:bg-indigo-400 active:bg-indigo-600"
              "rounded"
              "shadow-md"
              "focus:outline-none"
              (when disabled?
                "pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn RedButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge (merge {:class
             ["rounded"
              "shadow-md"
              "focus:outline-none"
              (if disabled?
                "pointer-events-none bg-blue-300"
                "bg-red-500 hover:bg-red-400 active:bg-red-600")]
             :on-click identity}
            attrs)
       attrs)
     child]))

(defn LightBlueButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:style
             {:background-color "#E5EEF9"}
             :class
             ["px-4 py-3"
              "rounded"
              "shadow-md"
              "focus:outline-none"
              "hover:bg-gray-200"
              "active:bg-gray-300"
              (when disabled?
                "pointer-events-none")]
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


(defn InfoTooltip [tooltip]
  [Tooltip
   {:title tooltip
    :size "small"}
   [InformationCircleIcon {:class "w-4 h-4 hover:text-gray-500"}]])


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
     "bg-gray-100"]
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
    ["text-xs"
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
      {:title "Copy"
       :size "small"}
      [CopyClipboardIcon
       {:class
        ["w-4 h-4 cursor-pointer"
         (or color "text-gray-500")
         (or background-color "")
         (or (some->> hover (apply str)) "hover:text-black")]
        :on-click
        (fn []
          (.writeText (.-clipboard js/navigator) text))}]]]))

(defn RangeNavigation [{:keys [page-count
                               page-num
                               first-label
                               first-href
                               last-label
                               last-href
                               previous-href
                               previous-disabled?
                               next-href
                               next-disabled?
                               ajax/status]}]

  (let [href #(when (not= :ajax.status/pending status) {:href %})
        action-style "block font-mono text-xs text-gray-800 hover:text-gray-500 hover:underline active:text-gray-900 uppercase"
        index-style "block font-mono text-xs text-gray-600 uppercase"]
    [:div.flex.space-x-8

     ;; Navigation
     ;; =============
     [:div.flex.items-center.space-x-4.px-2.py-1.border.border-gray-600.rounded
      ;; -- First
      [:a
       (href first-href)
       [:span {:class action-style} (or first-label "Latest")]]

      ;; -- Previous
      [:a
       (merge (href previous-href) (when previous-disabled?
                                      {:class "pointer-events-none"}))
       [:span {:class action-style} "Previous"]]

      ;; -- Next
      [:a
       (merge (href next-href) (when next-disabled?
                                  {:class "pointer-events-none"}))
       [:span {:class action-style} "Next"]]

      ;; -- Last
      [:a
       (href last-href)
       [:span {:class action-style} (or last-label "Earliest")]]]

     ;; Index
     ;; =============
     [:div.flex.items-center.space-x-4.px-2.py-1
      (case status
        :ajax.status/pending
        [:span {:class index-style}
         "Page _"]

        :ajax.status/success
        [:span {:class index-style}
         (str "Page " page-num " of " page-count)]

        "")]]))

(defn MarkdownCodeBlock [{:keys [value]}]
  [:pre.relative.max-w-xs.md:max-w-full.bg-blue-100.bg-opacity-25
   [:div.absolute.right-0.top-0.m-2
    [ClipboardCopy value {:color "text-black"
                          :hover "hover:opacity-50"}]]
   
   [:code.hljs.language-clojure.text-sm.rounded
    {:ref highlight-element}
    value]])

(defn Markdown [markdown]
  [:> ReactMarkdown
   {:components
    {:code (r/reactify-component MarkdownCodeBlock)}}
   markdown])

(def disclosure-button-shared
  ["w-full px-4 py-2 rounded-lg"
   "flex justify-between"
   "text-sm font-medium font-mono text-left"
   "focus:outline-none focus-visible:ring focus-visible:ring-opacity-75"])

(defn disclosure-button-colors [color]
  (map
    #(gstring/format % color)
    #{"text-%s-900"
      "bg-%s-100"
      "hover:bg-%s-200"
      "focus-visible:ring-%s-500"}))

(defn disclosure-button 
  "Returns a headlessui/Disclosure.Button 
   which can be used with the Disclosure component."
  [{:keys [text color on-click] :or {on-click identity}}]
  (fn [{:keys [open?]}]
    [:> headlessui/Disclosure.Button
     {:as react/Fragment
      :className
      (str/join " " (into disclosure-button-shared (disclosure-button-colors color)))}
     [:button.w-full
      {:on-click #(on-click (not open?))}
      [:div.flex.flex-1.justify-between
       [:span.text-xs
        text]
       [IconChevronUp
        {:class
         ["w-4 h-4"
          (gstring/format "text-%s-500" color)
          (when open? "transform rotate-180")]}]]]]))

(defn Disclosure [{:keys [DisclosureButton]} children]  
  [:> headlessui/Disclosure
   (fn [^js props]
     (r/as-element
       [:<>
        ;; Open & close.
        [DisclosureButton
         {:open? (.-open props)}]

        ;; Show children.
        [:> headlessui/Disclosure.Panel
         {:className "px-4 pb-2 text-sm text-gray-500"}

         (if (fn? children)
           (children props)
           children)]]))])
