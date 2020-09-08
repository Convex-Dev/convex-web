(ns convex-web.site.welcome
  (:require [convex-web.site.markdown :as markdown]
            [convex-web.site.gui :as gui]

            [reagent.core :as reagent]
            [reitit.frontend.easy :as rfe]

            ["@tailwindui/react" :refer [Transition]]))

(defn NavButton [text href]
  [:a.font-mono.text-base.hover:text-gray-500.px-4.py-2
   {:href href}
   text])

(defn DropdownButton [{:keys [text on-click]}]
  [:button.inline-flex
   {:class
    "inline-flex justify-center
     w-full
     px-4 py-2
     bg-white
     leading-5
     font-mono font-medium hover:text-gray-500
     focus:outline-none focus:border-blue-300 focus:shadow-outline-blue
     active:bg-gray-50 active:text-gray-800
     transition ease-in-out duration-150"
    :on-click (or on-click identity)}

   text

   [:svg.-mr-1.ml-2.h-5.w-5
    {:viewBox "0 0 20 20"
     :fill "currentColor"}
    [:path
     {:fill-rule "evenodd"
      :d "M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
      :clip-rule "evenodd"}]]])

(defn Dropdown [{:keys [text items]}]
  (let [show? (reagent/atom false)]
    (fn [{:keys [text items]}]
      [:div.relative.inline-block.text-left.text-base.text-black
       [:div

        [DropdownButton
         {:text text
          :on-click #(swap! show? not)}]

        [gui/Transition
         (merge gui/dropdown-transition {:show? @show?})
         [gui/Dismissible {:on-dismiss #(reset! show? false)}
          [:div.origin-top-right.absolute.right-0.mt-2.w-56.rounded-md.shadow-lg
           [:div.rounded-md.bg-white.shadow-xs
            [:div.py-1.font-mono
             {:role "menu"
              :aria-orientation "vertical"
              :aria-labelledby "options-menu"}

             (for [{:keys [text href]} items]
               ^{:key text}
               [:a.block.px-4.py-2.leading-5.hover:bg-gray-100.hover:text-gray-900.focus:outline-none.focus:bg-gray-100.focus:text-gray-900
                {:href href}
                text])]]]]]]])))

(defn Nav []
  [:div.h-16.flex.items-center.justify-between.px-12

   ;; -- Logo
   [:a {:href (rfe/href :route-name/welcome)}
    [:div.flex.items-center
     [gui/ConvexLogo {:width "28px" :height "32px"}]
     [:span.font-mono.text-xl.ml-4.leading-none "Convex"]]]

   [:div.flex.items-center.space-x-4
    ;; -- Guides
    [Dropdown
     {:text "Guides"
      :items
      [{:text "Concepts"
        :href (rfe/href :route-name/documentation-concepts)}

       {:text "Getting Started"
        :href (rfe/href :route-name/documentation-getting-started)}

       {:text "Tutorial"
        :href (rfe/href :route-name/documentation-tutorial)}

       {:text "Reference"
        :href (rfe/href :route-name/documentation-reference)}]}]

    ;; -- Sandbox
    [NavButton "Sandbox" (rfe/href :route-name/sandbox)]

    ;; -- Tools
    [Dropdown
     {:text "Tools"
      :items
      [{:text "Wallet"
        :href (rfe/href :route-name/wallet)}

       {:text "Faucet"
        :href (rfe/href :route-name/faucet)}]}]

    ;; -- Explorer
    [Dropdown
     {:text "Explorer"
      :items
      [{:text "Accounts"
        :href (rfe/href :route-name/accounts-explorer)}

       {:text "Blocks"
        :href (rfe/href :route-name/block-coll-explorer)}

       {:text "Transactions"
        :href (rfe/href :route-name/transactions-explorer)}]}]

    ;; -- About
    [Dropdown
     {:text "About"
      :items
      [{:text "FAQ"
        :href (rfe/href :route-name/accounts-explorer)}

       {:text "Concepts"
        :href (rfe/href :route-name/block-coll-explorer)}

       {:text "White Paper"
        :href (rfe/href :route-name/transactions-explorer)}

       {:text "Roadmap"
        :href (rfe/href :route-name/transactions-explorer)}]}]]])

(defn WelcomePage [_ _ _]
  (let [marketing-vertical ["w-1/2 flex flex-col justify-center space-y-8"]
        marketing-bullets ["flex flex-col space-y-4 text-base"]
        marketing-copy ["text-xl text-gray-700 leading-8"]]
    [:div.w-full.max-w-screen-xl.mx-auto

     [Nav]

     [:div.flex.flex-col.flex-1.items-center.justify-center.rounded
      {:style
       {:height "640px"
        :background-color "#F3F9FE"}}

      [gui/ConvexLogo {:width "56px" :height "64px"}]

      [:div.flex.flex-col.items-center.max-w-screen-md
       [:span.font-mono.text-6xl.mt-10
        "Building the Future"]

       [:div.flex.flex-col.items-center.text-xl.text-gray-800.leading-8.mt-10
        [:p "Convex is a global platform for trusted applications and digital assets."]
        [:p "Write amazing code with the most powerful platform for smart contracts and test your ideas live in the web browser — no additional installations required."]]]]

     [:div.flex.flex-1.justify-center.my-14
      [:span.inline-block.font-mono.text-center.text-4xl
       {:class "w-4/5"
        :style
        {:color "#62A6E1"}}
       "The tools to build the next generation of digital assets and applications are here."]]


     ;; Convex is flexible
     ;; =========================
     [:div.w-full.flex.mb-40.space-x-8

      ;; -- Image
      [:div {:class "w-1/2"}
       [:img {:src "images/convex_flexible.png"}]]

      ;; -- Copy
      [:div {:class marketing-vertical}

       [:span.font-mono.text-4xl "Convex is Flexible"]

       [:p.text-xl.leading-8
        "Convex is well suited as a platform for applications that need to be
         100% secure and also publicly verifiable (both in terms of data and
         application behaviour), such as:"]

       [:div {:class marketing-bullets}
        [:span.font-mono "Public registries and databases"]

        [:span.font-mono "Digital currencies"]

        [:span.font-mono "Prediction markets"]

        [:span.font-mono "Smart contracts for managing digital assets"]

        [:span.font-mono "Immutable provenance records"]]]]

     ;; Convex is fast
     ;; =========================
     [:div.w-full.flex.mb-40.space-x-8

      ;; -- Copy
      [:div {:class marketing-vertical}

       [:span.font-mono.text-4xl "Convex is Fast"]

       [:p {:class marketing-copy}
        "With a novel consensus algorithm, Convex is able to execute
         decentralised applications at internet scale. Using normal consumer
         grade hardware and network bandwidth the network can achieve:"]

       [:div {:class marketing-bullets}
        [:span.font-mono
         "Thousands of digitally signed transactions per second (more than the
          1,700 transactions per second typically handled by the VISA network)"]

        [:span.font-mono
         "Ability to execute over a million operations per second on the CVM"]

        [:span.font-mono
         "Low latency (around 1 second for global consensus)"]]

       [:p {:class marketing-copy}
        "In the future, it will be possible to extend scalability even further
         through proven techniques such as sharding, state channel or side
         chains."]]

      ;; -- Image
      [:div {:class "w-1/2"}
       [:img {:src "images/convex_fast.png"}]]]


     ;; Convex is fun
     ;; =========================
     [:div.w-full.flex.mb-40

      ;; -- Image
      [:div {:class "w-1/2"}
       [:img {:src "images/convex_fun.png"}]]

      ;; -- Copy
      [:div {:class marketing-vertical}

       [:span.font-mono.text-4xl "Convex is Fun"]

       [:p {:class marketing-copy}
        "We believe in providing a powerful, interactive environment for
         development in Convex that enables high productivity while maintaining
         secure coding principles."]

       [:p {:class marketing-copy}
        "Convex provides an interactive REPL allowing users to code directly
         on the Convex platform using Convex Lisp."]]]

     ]))

(def welcome-page
  #:page {:id :page.id/welcome
          :title "Welcome"
          :component #'WelcomePage
          :scaffolding? false})
