(ns convex-web.site.sandbox.hiccup
  "See convex-web.site.sandbox.hiccup-test for examples."
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::element-tag
  #{:text :command :h-box :v-box})

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