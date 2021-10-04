(ns convex-web.site.sandbox.renderer
  (:require
   [cljs.spec.alpha :as s]
   
   [convex-web.site.sandbox.hiccup :as hiccup]))

(defmulti compile* :tag)

(defmethod compile* :text
  [{:keys [content]}]
  ;; Text's content is the first number/string/element.
  (let [[_ content-body] (first content)]
    [:span
     (str content-body)]))

(defmethod compile* :command
  [{:keys [content]}]
  ;; Command's content is the first number/string/element.
  (let [[_ content-body] (first content)]
    [:button
     {:on-click
      (fn [])}
     (str content-body)]))

(defmethod compile* :h-box
  [{:keys [content]}]
  (into [:div.flex.flex-row] (map compile* content)))

(defn compile [markup]
  (let [conformed (s/conform ::hiccup/element markup)]
    (if (= conformed :clojure.spec.alpha/invalid)
      (throw (ex-info "Invalid markup."
               {:explain (s/explain-str ::element markup)}))
      (compile* conformed))))