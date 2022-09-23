(ns convex.web.wallet

  "Each wallet is designated by a session and can host one or several accounts."

  (:require [convex-web.convex  :as $.web.convex]
            [convex.web.session :as $.web.session]))


;;;;;;;;;;


(defn account-key-pair

  "Finds the key pair of the given account."
  
  [db {address    :convex-web/address
       id-session :convex-web.session/id}]

  (reduce (fn [_acc account]
            (when (= ($.web.convex/address address)
                     ($.web.convex/address (account :convex-web.account/address)))
              (reduced (account :convex-web.account/key-pair))))
          nil
          (-> db
              ($.web.session/find-session id-session)
              (:convex-web.session/wallet))))
