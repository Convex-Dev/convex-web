(ns convex-web.site.videos)

(defn VideosPage [_ _ _]
  (let [iframe {:width "405.33"
                :height "200.58"
                :allow "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                :allowfullscreen true}]

    [:div.grid.grid-cols-3.gap-12

     ;; -- Interactive REPL Explained
     [:iframe
      (merge iframe
        {:title "Interactive REPL Explained"
         :src "https://www.youtube.com/embed/d_4pR_GWJsM?si=yIR-u_3oh0-liEuB" })]

     ;; -- Importance of Runtime
     [:iframe
      (merge iframe
        {:title "Importance of Runtime"
         :src "https://www.youtube.com/embed/mNxuVgjotEM?si=WZ-UB-Zv-zYr7gQ-" })]]))

(def videos-page
  #:page
  {:id :page.id/videos
   :title "Video"
   :template :marketing
   :component #'VideosPage})
