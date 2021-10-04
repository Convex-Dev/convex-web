(ns convex-web.site.sandbox.hiccup
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::element-tag
  #{:text :button})

(s/def ::element-attributes
  (s/map-of keyword? any?))

(s/def ::element-content
  (s/+ (s/or
         :terminal (s/or
                     :string string?
                     :number number?)

         :element ::element)))

(s/def ::element
  (s/cat
    :tag ::element-tag
    :attributes (s/? ::element-attributes)
    :content ::element-content))