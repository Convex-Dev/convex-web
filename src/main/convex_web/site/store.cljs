(ns convex-web.site.store
  (:require [convex-web.site.runtime :refer [disp sub]]
            [convex-web.site.backend :as backend]

            [reagent.ratom :refer [make-reaction]]
            [re-frame.core :as re-frame]))

;; -- Account

(defn find-account [db address]
  (some
    (fn [account]
      (when (= address (:convex-web.account/address account))
        account))
    (get-in db [:site/store :convex-web/accounts])))

(re-frame/reg-event-db :store/!add-account
  (fn [db [_ account]]
    (update-in db [:site/store :convex-web/accounts] (fnil conj []) account)))

(re-frame/reg-sub-raw :store/?account
  (fn [app-db [_ address]]
    (backend/GET-account address {:handler (fn [account]
                                             (disp :store/!add-account account))})

    (make-reaction
      (fn []
        (find-account @app-db address)))))

(defn ?account [address]
  (sub :store/?account address))
