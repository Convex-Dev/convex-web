(ns convex-web.config
  #?(:clj
     (:require [aero.core :as aero])))

(def default-range 15)

(def max-range 25)

(def max-faucet-amount 100000000)

(def faucet-wait-millis
  "Milliseconds a user has to wait, since last request, to submit a Faucet."
  (* 1000 60 5))

#?(:clj
   (defmethod aero/reader 'system/property [_ _ value]
     (System/getProperty value)))

#?(:clj 
   (defn read-config [profile]
     (aero/read-config "convex-web.edn" {:profile profile})))
