(ns convex-web.site.blockchain
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-event-db :blockchain/!transact
  (fn [db [_ block]]
    (update-in db [:site/blockchain :blockchain/blocks] (fnil conj []) block)))

(re-frame/reg-sub :blockchain/?blocks
  (fn [db _]
    (get-in db [:site/blockchain :blockchain/blocks])))