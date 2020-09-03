(ns convex-web.site.welcome
  (:require [convex-web.site.markdown :as markdown]
            [convex-web.site.gui :as gui]))

(defn Nav []
  [:div.h-16.flex.justify-between

   [:div.flex.items-center
    [gui/ConvexLogo {:width "28px" :height "32px"}]
    [:span.font-mono.text-xl.ml-4.leading-none "Convex"]]

   [:div.flex
    [:span "Guides"]]])

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
