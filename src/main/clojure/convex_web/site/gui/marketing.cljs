(ns convex-web.site.gui.marketing
  (:require 
   [reitit.frontend.easy :as rfe]
   [reagent.core :as reagent]
   
   [convex-web.site.stack :as stack]   
   [convex-web.site.gui :as gui]
   
   ["@heroicons/react/solid" :refer [MenuIcon]]))

(defn nav []
  {:concepts
   {:text "CONCEPTS"
    :items
    [{:text "Vision"
      :href (rfe/href :route-name/vision)}

     {:text "Glossary"
      :href (rfe/href :route-name/glossary)}

     {:text "FAQ"
      :href (rfe/href :route-name/faq)}]}

   :documentation
   {:text "DOCUMENTATION"
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
   {:text "TOOLS"
    :items
    [{:text "Wallet"
      :href (rfe/href :route-name/wallet)}

     {:text "Faucet"
      :href (rfe/href :route-name/faucet)}

     {:text "Transfer"
      :href (rfe/href :route-name/transfer)}]}

   :explorer
   {:text "EXPLORER"
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
   {:text "ABOUT"
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
  [:a
   {:class ["text-base text-blue-800 hover:text-blue-900 uppercase"
            "hover:bg-gray-100 active:bg-gray-200"
            "transition duration-150 ease-in-out"
            "px-4 py-2 rounded"]
    :href href}
   text])

(defn DropdownButton [{:keys [text on-click]}]
  [:button.inline-flex
   {:class
    "inline-flex justify-center
     w-full
     px-4 py-2
     bg-white
     leading-5
     font-medium text-blue-800 hover:text-blue-900
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
      [:div.relative.inline-block.text-left.text-base.text-black.z-10
       [:div

        [DropdownButton
         {:text text
          :on-click #(swap! show? not)}]

        [gui/Transition
         (merge gui/dropdown-transition {:show? @show?})
         [gui/Dismissible {:on-dismiss #(reset! show? false)}
          [:div.origin-top-right.absolute.right-0.mt-2.w-56.rounded-md.shadow-lg.z-10
           [:div.rounded-md.bg-white.shadow-xs
            [:div.py-1
             {:role "menu"
              :aria-orientation "vertical"
              :aria-labelledby "options-menu"}

             (for [{:keys [text href]} items]
               ^{:key text}
               [:a.block.px-4.py-2.leading-5.hover:bg-blue-100.hover:bg-opacity-50.hover:text-gray-900.focus:outline-none.focus:bg-gray-100.focus:text-gray-900
                {:href href}
                text])]]]]]]])))

(defn NavDesktop [nav]
  [:div.hidden.md:flex.items-center.space-x-4
   
   (for [{:keys [text href]} nav]
     ^{:key text}
     [NavButton text href])])

(defn Nav []
  (reagent/with-let [show?-ref (reagent/atom false)]
    (let [items [{:text "Technology"
                  :href (rfe/href :route-name/technology)}
                 
                 {:text "Use Cases"
                  :href (rfe/href :route-name/use-cases)}
                 
                 {:text "Ecosystem"
                  :href (rfe/href :route-name/ecosystem)}
                 
                 {:text "Team"
                  :href (rfe/href :route-name/team)}
                 
                 {:text "About"
                  :href (rfe/href :route-name/about)}
                 
                 {:text "Developer"
                  :href (rfe/href :route-name/developer)}]]
      [:nav.h-16.flex.items-center.justify-between.pl-10.pr-4.border-b.border-gray-100
       
       [:div.flex.flex-row.justify-between.items-center.flex-1
        
        ;; -- Logo
        [:a {:href (rfe/href :route-name/welcome)}
         [:div.flex.items-center.space-x-4
          
          [gui/ConvexLogo {:width "28px" :height "32px"}]
          
          [:span.text-2xl.font-bold.leading-none.text-blue-800 "CONVEX"]]]
        
        ;; -- Menu (mobile only)
        [:div.relative.inline-block.text-left.text-base.text-black.z-10
         [:button.md:hidden.rounded.p-2.shadow-md.transition.ease-in-out.duration-150
          {:class
           (if @show?-ref 
             "bg-blue-200"
             "bg-blue-50")}
          [:> MenuIcon 
           {:className "h-5 w-5 text-blue-800"
            :on-click #(swap! show?-ref not)}]]
         
         [gui/Transition
          (merge gui/dropdown-transition {:show? @show?-ref})
          [gui/Dismissible {:on-dismiss #(reset! show?-ref false)}
           [:div.origin-top-right.absolute.right-0.mt-2.w-56.rounded-md.shadow-lg.z-10
            [:div.rounded-md.bg-white.shadow-xs
             [:div.py-1
              {:role "menu"
               :aria-orientation "vertical"
               :aria-labelledby "options-menu"}
              
              (for [{:keys [text href]} items]
                ^{:key text}
                [:a.block.px-4.py-2.leading-5.hover:bg-blue-100.hover:bg-opacity-50.hover:text-gray-900.focus:outline-none.focus:bg-gray-100.focus:text-gray-900
                 {:href href
                  :on-click #(reset! show?-ref false)}
                 text])]]]]]]]
       
       ;; -- Nav (desktop only)
       [NavDesktop items]])))

(defn BottomNavMenu [{:keys [text items]}]
  [:div.flex.flex-col.space-y-3.mb-10
   
   [:span.text-xl.text-blue-200.uppercase text]
   
   [:div.flex.flex-col.space-y-2
    (for [{:keys [text href]} items]
      ^{:key text}
      [:a {:href href}
       [:span.text-lg.text-gray-400.hover:text-gray-200.active:text-gray-600 text]])]])

(defn BottomNavMenuSocial []
  [:div.flex.flex-col.space-y-3.mb-10
   
   [:span.text-xl.text-blue-200.uppercase "Community"]
   
   [:div.flex.space-x-2
    (for [{:keys [href src]} [{:src "/social_discord.png"
                               :href "https://discord.gg/fsnCxEM"}
                              
                              {:src "/social_github.png"
                               :href "https://github.com/Convex-Dev"}
                              
                              {:src "/social_twitter.png"
                               :href "https://twitter.com/convex_world"}
                              
                              {:src "/social_linkedin.png"
                               :href "https://www.linkedin.com/company/convex-foundation/"}
                              
                              {:src "/social_youtube.png"
                               :href "https://www.youtube.com/channel/UCrasexr20HzFZS-xgnYcoug/featured"}]]
      ^{:key href}
      [:a {:href href}
       [:img.object-scale-down.w-10.h-10
        {:src src}]])]])

(defn BottomNav [nav]
  [:div.lg:flex.lg:space-x-32.p-12.bg-gray-900

   (let [{:keys [concepts documentation tools explorer about]} nav]
     [:<>
      [BottomNavMenu concepts]
      [BottomNavMenu documentation]
      [BottomNavMenu tools]
      [BottomNavMenu explorer]
      [BottomNavMenu about]
      [BottomNavMenuSocial]])])

(defn Copyrigth []
  [:div.flex.flex-col.items-center.space-y-4.bg-gray-900.p-2
   
   [:span.block.text-gray-500.text-sm
    "© Copyright 2021 CONVEX FOUNDATION"]])