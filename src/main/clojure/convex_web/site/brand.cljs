(ns convex-web.site.brand)

(defn BrandPage
  [_frame _state _set-state]
  [:div
   [:p
    "Brand page"]])

(def brand-page
  #:page {:id :page.id/brand
          :component #'BrandPage
          :template :marketing})
