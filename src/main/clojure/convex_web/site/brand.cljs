(ns convex-web.site.brand)

(defn BrandPage
  [_frame _state _set-state]

  [:div.flex.flex-col

   [:div.flex.flex-col
    [:h1
     "Logo"]

    [:p
     "This is the Convex logo in the official colors and layout versions. The logo should not be placed on top of other objects or distorted and skewed in any way.  Please refer to the Brand Manual for more complete details."]]

   ;; -- Download Brand Manual
   [:a
    {:class
     ["h-[55.75px] w-[220px]"
      "inline-flex items-center justify-center self-center"
      "bg-white hover:bg-gray-100 focus:bg-gray-300"
      "border-2 border-convex-dark-blue rounded"
      "mt-36"]
     :href "https://raw.githubusercontent.com/Convex-Dev/design/main/papers/convex-whitepaper.pdf"}
    [:span.text-base.text-convex-dark-blue
     "Download Brand Manual"]

    ]])

(def brand-page
  #:page {:id :page.id/brand
          :component #'BrandPage
          :template :marketing})
