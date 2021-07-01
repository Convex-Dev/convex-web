(ns convex-web.site.gui.marketing
  (:require [reitit.frontend.easy :as rfe]
            [reagent.core :as reagent]
            
            [convex-web.site.gui :as gui]))

(defn nav []
  {:concepts
   {:text "Concepts"
    :items
    [{:text "Vision"
      :href (rfe/href :route-name/vision)}

     {:text "Glossary"
      :href (rfe/href :route-name/glossary)}

     {:text "FAQ"
      :href (rfe/href :route-name/faq)}]}

   :documentation
   {:text "Documentation"
    :items
    [{:text "Getting Started"
      :href (rfe/href :route-name/documentation-getting-started)}

     {:text "Lisp Guide"
      :href (rfe/href :route-name/documentation-tutorial)}

     {:text "Advanced Topics"
      :href (rfe/href :route-name/advanced-topics)}

     {:text "Reference"
      :href (rfe/href :route-name/documentation-reference)}

     {:text "Client API"
      :href (rfe/href :route-name/client-api)}]}

   :tools
   {:text "Tools"
    :items
    [{:text "Wallet"
      :href (rfe/href :route-name/wallet)}

     {:text "Faucet"
      :href (rfe/href :route-name/faucet)}

     {:text "Transfer"
      :href (rfe/href :route-name/transfer)}]}

   :explorer
   {:text "Explorer"
    :items
    [{:text "Accounts"
      :href (rfe/href :route-name/accounts-explorer)}

     {:text "Blocks"
      :href (rfe/href :route-name/blocks)}

     {:text "Status"
      :href (rfe/href :route-name/state)}

     {:text "Transactions"
      :href (rfe/href :route-name/transactions)}]}

   :about
   {:text "About"
    :items
    [#_{:text "Concepts"
        :href (rfe/href :route-name/concepts)}

     #_{:text "White Paper"
        :href (rfe/href :route-name/white-paper)}

     {:text "Team"
      :href (rfe/href :route-name/team)}
     
     {:text "Get Involved"
      :href (rfe/href :route-name/get-involved)}

     #_{:text "Roadmap"
        :href (rfe/href :route-name/roadmap)}

     #_{:text "Convex Foundation"
        :href (rfe/href :route-name/convex-foundation)}]}})


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
               [:a.block.px-4.py-2.leading-5.hover:bg-blue-100.hover:bg-opacity-50.hover:text-gray-900.focus:outline-none.focus:bg-gray-100.focus:text-gray-900
                {:href href}
                text])]]]]]]])))

(defn Nav [nav]
  [:div.h-16.flex.items-center.justify-between.px-10.border-b.border-gray-100
   
   ;; -- Logo
   [:a {:href (rfe/href :route-name/welcome)}
    [:div.flex.items-center
     [gui/ConvexLogo {:width "28px" :height "32px"}]
     [:span.font-mono.text-xl.ml-4.leading-none "Convex"]]]
   
   [:div.flex.items-center.space-x-4
    ;; -- Concepts
    [Dropdown
     (:concepts nav)]
    
    ;; -- Documentation
    [Dropdown
     (:documentation nav)]
    
    ;; -- Sandbox
    [NavButton "Sandbox" (rfe/href :route-name/sandbox)]
    
    ;; -- Tools
    [Dropdown
     (:tools nav)]
    
    ;; -- Explorer
    [Dropdown
     (:explorer nav)]
    
    ;; -- About
    [Dropdown
     (:about nav)]]])

(defn BottomNavMenu [{:keys [text items]}]
  [:div.flex.flex-col.space-y-3.mb-10
   
   [:span.font-mono.text-base.text-black text]
   
   [:div.flex.flex-col.space-y-2
    (for [{:keys [text href]} items]
      ^{:key text}
      [:a {:href href}
       [:span.text-sm.text-gray-600.hover:text-gray-400.active:text-gray-800 text]])]])

(defn BottomNav [nav]
  [:div.lg:flex.lg:space-x-32

   (let [{:keys [concepts documentation tools explorer about]} nav]
     [:<>
      [BottomNavMenu concepts]
      [BottomNavMenu documentation]
      [BottomNavMenu tools]
      [BottomNavMenu explorer]
      [BottomNavMenu about]])])

(defn Copyrigth []
  [:div.flex.flex-col.items-center.space-y-4.mb-8
   
   [:a
    {:href "https://github.com/Convex-Dev"
     :target "_blank"}
    [:div.p-2.bg-gray-100.hover:bg-opacity-50.active:bg-gray-200.rounded-md
     [gui/GitHubIcon]]]
   
   [:span.block.text-gray-500.text-sm
    "© Copyright 2021 The Convex Foundation."]])