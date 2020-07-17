(ns convex-web.site.welcome
  (:require [reitit.frontend.easy :as rfe]
            [convex-web.site.backend :as backend]
            [convex-web.site.gui :as gui]))

(defn WelcomePage [_ {:keys [ajax/status contents]} _]
  [:div.flex.flex-1
   (case status
     :ajax.status/pending
     [:div.flex.flex-1.items-center.justify-center
      [gui/Spinner]]

     :ajax.status/error
     [:div.py-10.px-10
      [:span "Error"]]

     :ajax.status/success
     [:div.mt-4.mx-10
      (for [{:keys [name content]} contents]
        ^{:key name}
        [:article.prose.mb-10
         {:id name}
         [gui/Markdown content]])]

     [:div])])

(def welcome-page
  #:page {:id :page.id/welcome
          :title "Welcome"
          :component #'WelcomePage
          :on-push
          (fn [_ _ set-state]
            (set-state assoc :ajax/status :ajax.status/pending)

            (backend/GET-markdown-page
              :welcome
              {:handler
               (fn [contents]
                 (set-state assoc
                            :ajax/status :ajax.status/success
                            :contents contents))

               :error-handler
               (fn [error]
                 (set-state assoc
                            :ajax/status :ajax.status/error
                            :ajax/error error))}))})
