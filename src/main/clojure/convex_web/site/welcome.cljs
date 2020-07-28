(ns convex-web.site.welcome
  (:require [convex-web.site.markdown :as markdown]))

(defn WelcomePage [_ state _]
  [markdown/Markdown (assoc-in state [:markdown :toc?] false)])

(def welcome-page
  #:page {:id :page.id/welcome
          :title "Welcome"
          :component #'WelcomePage
          :on-push (markdown/hook-fn :welcome)})
