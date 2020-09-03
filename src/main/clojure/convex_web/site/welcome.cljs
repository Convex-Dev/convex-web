(ns convex-web.site.welcome
  (:require [convex-web.site.markdown :as markdown]
            [convex-web.site.gui :as gui]

            [reagent.core :as reagent]
            [reitit.frontend.easy :as rfe]

            ["@tailwindui/react" :refer [Transition]]))

(defn Nav []
  (let [state-ref (reagent/atom {})]
    (fn []
      [:div.h-16.flex.justify-between

       [:div.flex.items-center
        [gui/ConvexLogo {:width "28px" :height "32px"}]
        [:span.font-mono.text-xl.ml-4.leading-none "Convex"]]

       [:div.flex.items-center

        ;; Dropdown
        [:div.relative.inline-block.text-left

         [:div

          ;; Button
          [:span.rounded-md.shadow-sm
           [:button.inline-flex
            {:class
             "inline-flex justify-center
              w-full
              px-4 py-2
              bg-white
              text-sm
              leading-5
              font-medium text-gray-700 hover:text-gray-500
              focus:outline-none focus:border-blue-300 focus:shadow-outline-blue
              active:bg-gray-50 active:text-gray-800
              transition ease-in-out duration-150"
             :on-click #(swap! state-ref update :visible? not)}

            "Guides"

            [:svg.-mr-1.ml-2.h-5.w-5
             {:viewBox "0 0 20 20"
              :fill "currentColor"}
             [:path
              {:fill-rule "evenodd"
               :d "M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
               :clip-rule "evenodd"}]]]]


          [:> Transition
           {:show (or (:visible? @state-ref) false)
            :enter "transition ease-out duration-100"
            :enterFrom "transform opacity-0 scale-95"
            :enterTo "transform opacity-100 scale-100"
            :leave "transition ease-in duration-75"
            :leaveFrom "transform opacity-100 scale-100"
            :leaveTo "transform opacity-0 scale-95"}
           [:div.origin-top-right.absolute.right-0.mt-2.w-56.rounded-md.shadow-lg

            [:div.rounded-md.bg-white.shadow-xs

             [:div.py-1 {:role "menu" :aria-orientation "vertical" :aria-labelledby "options-menu"}

              [:a.block.px-4.py-2.text-sm.leading-5.text-gray-700.hover:bg-gray-100.hover:text-gray-900.focus:outline-none.focus:bg-gray-100.focus:text-gray-900
               {:href (rfe/href :route-name/documentation-concepts)}
               "Concepts"]

              [:a.block.px-4.py-2.text-sm.leading-5.text-gray-700.hover:bg-gray-100.hover:text-gray-900.focus:outline-none.focus:bg-gray-100.focus:text-gray-900
               {:href (rfe/href :route-name/documentation-getting-started)}
               "Getting Started"]

              [:a.block.px-4.py-2.text-sm.leading-5.text-gray-700.hover:bg-gray-100.hover:text-gray-900.focus:outline-none.focus:bg-gray-100.focus:text-gray-900
               {:href (rfe/href :route-name/documentation-tutorial)}
               "Tutorial"]
              ]]]
           ]]

         ]]])))

(defn WelcomePage [_ state _]
  [:div.w-full.max-w-screen-xl.mx-auto

   [Nav]

   [:div.flex.flex-col.flex-1.items-center.justify-center.rounded
    {:style
     {:height "640px"
      :background-color "#F3F9FE"}}

    [gui/ConvexLogo {:width "56px" :height "64px"}]

    [:span.font-mono.text-6xl.pt-10
     "Building the Future"]

    [:span "Convex is a global platform for trusted applications and digital assets."]
    [:span "Write amazing code with the most powerful platform for smart contracts and test your ideas live in the web browser — no additional installations required."]]

   [:span.font-mono.text-4xl
    {:style
     {:color "#62A6E1"}}
    "The tools to build the next generation of digital assets and applications are here."]])

(def welcome-page
  #:page {:id :page.id/welcome
          :title "Welcome"
          :component #'WelcomePage
          :on-push (markdown/hook-fn :welcome)})
