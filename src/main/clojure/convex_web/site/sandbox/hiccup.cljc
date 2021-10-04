(ns convex-web.site.sandbox.hiccup
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::element-tag
  #{:text :button})

(s/def ::element-attributes
  (s/map-of keyword? any?))

(s/def ::element-content
  (s/or
    :string string?
    :number number?
    :element ::element))

(s/def ::element
  (s/cat
    :tag ::element-tag
    :attributes (s/? ::element-attributes)
    :content (s/* ::element-content)))

(defmulti compile* :tag)

(defmethod compile* :text
  [{:keys [content]}]
  (let [[content-type content-body] (first content)]
    [:span
     (case content-type
       :string
       content-body

       :number
       (str content-body)

       :element
       (str content-body))]))

(defn compile [hiccup]
  (let [conformed (s/conform ::element hiccup)]
    (if (= conformed :clojure.spec.alpha/invalid)
      (throw (ex-info "Invalid hiccup."
               {:hiccup hiccup
                :explain (s/explain-str ::element hiccup)}))
      (compile* conformed))))