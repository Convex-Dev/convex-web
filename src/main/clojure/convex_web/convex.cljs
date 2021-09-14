(ns convex-web.convex
  (:require
   [clojure.string :as str]))

(def library-documentation
  {"asset.box" 
   ""
   
   "asset.nft-tokens" 
   ""
   
   "asset.simple-nft" 
   ""
   
   "convex.asset" 
   ""
   
   "convex.core" 
   ""
   
   "convex.fungible"
   ""
   
   "convex.nft-tokens"
   ""
   
   "convex.registry"
   ""
   
   "convex.trust" 
   "This library is based on the Reference Monitor security model.
   The library works with 'Trust Monitors', which are a design 
   pattern of for reference monitors on the Convex platform."
   
   "convex.trusted-oracle"
   ""
   
   "torus.exchange"
   ""})

(defn address
  "Returns address `x` as a number.

  Throws ex-info if it can't create an address from `x`."
  [x]
  (cond
    (number? x)
    x

    (string? x)
    (js/parseInt (str/replace x "#" ""))

    :else
    (throw (ex-info (str "Can't create address from " x) {:x x}))))
