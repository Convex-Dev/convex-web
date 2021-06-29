(ns convex-web.site.team
  (:require [convex-web.site.gui.marketing :as marketing]))

(defn TeamPage [_ _ _]
  [:div
   
   [marketing/Nav]
   
   ])

(def team-page
  #:page {:id :page.id/team
          :component #'TeamPage
          :scaffolding? false})