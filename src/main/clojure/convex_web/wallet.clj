(ns convex-web.wallet
  (:require
   [convex-web.session :as session]
   [convex-web.convex :as convex]))

(defn account-key-pair [db {sid :convex-web.session/id
                            address :convex-web/address}]
  (let [session (session/find-session db sid)

        {wallet :convex-web.session/wallet} session]
    (reduce
      (fn [_ account]
        (let[{account-address :convex-web.account/address
              account-key-pair :convex-web.account/key-pair} account]
          (when (= (convex/address address) (convex/address account-address))
            (reduced account-key-pair))))
      nil
      wallet)))