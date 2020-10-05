(ns convex-web.convex.datafy
  (:require [clojure.core.protocols :as p]
            [clojure.datafy :refer [datafy]])
  (:import (convex.core.data Keyword Symbol AList AVector AMap ASet Syntax Address ABlob)))

(extend-type Keyword
  p/Datafiable
  (datafy [x]
    (keyword (.getName x))))

(extend-type Symbol
  p/Datafiable
  (datafy [x]
    (symbol (some-> x (.getNamespace) (.getName)) (.getName x))))

(extend-type AList
  p/Datafiable
  (datafy [x]
    (map datafy x)))

(extend-type AVector
  p/Datafiable
  (datafy [x]
    (map datafy x)))

(extend-type AMap
  p/Datafiable
  (datafy [x]
    (reduce
      (fn [m [k v]]
        (assoc m (datafy k) (datafy v)))
      {}
      x)))

(extend-type ASet
  p/Datafiable
  (datafy [x]
    (into #{} (map datafy x))))

(extend-type Syntax
  p/Datafiable
  (datafy [x]
    (datafy (.getValue ^Syntax x))))

(extend-type Address
  p/Datafiable
  (datafy [x]
    {:hex-string (.toHexString x)
     :checksum-hex (.toChecksumHex x)}))

(extend-type ABlob
  p/Datafiable
  (datafy [x]
    {:length (.length ^ABlob x)
     :hex-string (.toHexString ^ABlob x)}))