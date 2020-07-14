(ns convex-web.site.welcome
  (:require [reitit.frontend.easy :as rfe]))

(defn WelcomePage [_ _ _]
  [:div.flex.flex-col.items-start.mx-10

   [:a.hover:underline.mt-6
    {:href (rfe/href :route-name/repl)}
    [:span "Try the REPL"]]

   [:a.hover:underline.mt-6
    {:href (rfe/href :route-name/wallet)}
    [:span "Go to Wallet"]]])

(def welcome-page
  #:page {:id :page.id/welcome
          :title "Welcome"
          :component #'WelcomePage})
