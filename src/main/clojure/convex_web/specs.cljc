(ns convex-web.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]

            [reitit.core]))

(s/def :convex-web/non-empty-string (s/and string? (complement str/blank?)))

(s/def :convex-web/address (s/and :convex-web/non-empty-string #(= 64 (count %))))

(s/def :convex-web/amount nat-int?)

(s/def :convex-web/sequence pos-int?)

;; -- Runtime

(s/def :runtime/interval-ref some?)

;; -- Ajax

(s/def :ajax/status #{:ajax.status/pending
                      :ajax.status/success
                      :ajax.status/error})

(s/def :ajax.error/status nat-int?)
(s/def :ajax.error/status-text string?)
(s/def :ajax.error/response any?)
(s/def :ajax.error/failure
  ;; If the failure had a valid response, it will be stored in the :response key.
  ;; If the error is :parse then the raw text of the response will be stored in :original-text.
  ;; Finally, if the server returned an error, and that then failed to parse,
  ;; it will return the error map, but add a key :parse-error that contains the parse failure.
  ;;
  ;; https://github.com/JulianBirch/cljs-ajax#error-responses
  #{;; an error on the server
    :error
    ;; the response from the server failed to parse
    :parse
    ;; the client aborted the request
    :aborted
    ;; the request timed out
    :timeout

    ;; not documented
    :failed})

(s/def :ajax/error (s/keys :req-un [:ajax.error/status
                                    :ajax.error/status-text
                                    :ajax.error/failure]
                           :opt-un [:ajax.error/response]))

(s/def :ajax/pending-status (s/and (s/keys :req [:ajax/status]) #(= :ajax.status/pending (:ajax/status %))))
(s/def :ajax/success-status (s/and (s/keys :req [:ajax/status]) #(= :ajax.status/success (:ajax/status %))))
(s/def :ajax/error-status (s/and (s/keys :req [:ajax/status :ajax/error]) #(= :ajax.status/error (:ajax/status %))))


;; -- Faucet

(s/def :convex-web.faucet/target :convex-web/address)
(s/def :convex-web.faucet/amount :convex-web/amount)

(s/def :convex-web/faucet (s/keys :req [:convex-web.faucet/target
                                        :convex-web.faucet/amount]))


;; -- Account Status

(s/def :convex-web.account-status/sequence nat-int?)
(s/def :convex-web.account-status/balance nat-int?)

(s/def :convex-web/account-status (s/keys :req [:convex-web.account-status/sequence
                                                :convex-web.account-status/balance]))


;; -- Account

(s/def :convex-web.account/status :convex-web/account-status)
(s/def :convex-web.account/address :convex-web/address)

(s/def :convex-web/account (s/keys :req [:convex-web.account/address]
                                   :opt [:convex-web.account/status]))

(s/def :convex-web/accounts (s/coll-of :convex-web/account))


;; -- Signed Data

(s/def :convex-web.signed-data/address :convex-web/address)
(s/def :convex-web.signed-data/value any?)

(s/def :convex-web/signed-data (s/keys :req [:convex-web.signed-data/address
                                             :convex-web.signed-data/value]))

;; -- Transfer

(s/def :convex-web.transfer/from :convex-web/address)
(s/def :convex-web.transfer/to :convex-web/address)
(s/def :convex-web.transfer/amount :convex-web/amount)

(s/def :convex-web/transfer (s/keys :req [:convex-web.transfer/from
                                          :convex-web.transfer/to
                                          :convex-web.transfer/amount]))

;; -- Query

(s/def :convex-web.query/source :convex-web/non-empty-string)

(s/def :convex-web/query
  (s/keys :req [:convex-web.query/source]))

;; -- Transaction

(s/def :convex-web.transaction/type #{:convex-web.transaction.type/invoke
                                      :convex-web.transaction.type/transfer})
(s/def :convex-web.transaction/target :convex-web/address)
(s/def :convex-web.transaction/amount :convex-web/amount)
(s/def :convex-web.transaction/source :convex-web/non-empty-string)
(s/def :convex-web.transaction/sequence :convex-web/sequence)
(s/def :convex-web.transaction/index nat-int?)

(defmulti transaction :convex-web.transaction/type)

(defmethod transaction :convex-web.transaction.type/transfer [_]
  (s/keys :req [:convex-web.transaction/type
                :convex-web.transaction/target
                :convex-web.transaction/amount]
          :opt [:convex-web.transaction/sequence]))

(defmethod transaction :convex-web.transaction.type/invoke [_]
  (s/keys :req [:convex-web.transaction/type
                :convex-web.transaction/source]
          :opt [:convex-web.transaction/sequence]))

(s/def :convex-web/transaction (s/multi-spec transaction :convex-web.transaction/type))

(s/def :convex-web/transaction-block-child (s/merge :convex-web/transaction (s/keys :req [:convex-web.transaction/index])))


;; -- Block

(s/def :convex-web.block/index nat-int?)
(s/def :convex-web.block/timestamp pos-int?)
(s/def :convex-web.block/transactions (s/coll-of :convex-web/signed-data :min-count 1))

(s/def :convex-web/block (s/keys :req [:convex-web.block/index]
                                 :opt [:convex-web.block/timestamp
                                       :convex-web.block/transactions]))

(s/def :convex-web/blocks (s/coll-of :convex-web/block))

;; -- Command

(s/def :convex-web.command/mode #{:convex-web.command.mode/query
                                  :convex-web.command.mode/transaction})

(s/def :convex-web.command/status #{:convex-web.command.status/running
                                    :convex-web.command.status/error
                                    :convex-web.command.status/success})

(s/def :convex-web.command/id pos-int?)
(s/def :convex-web.command/address :convex-web/address)
(s/def :convex-web.command/transaction :convex-web/transaction)
(s/def :convex-web.command/query :convex-web/query)
(s/def :convex-web.command/object any?)
(s/def :convex-web.command/error any?)

(defmulti incoming-command :convex-web.command/mode)

(defmethod incoming-command :convex-web.command.mode/query [_]
  (s/keys :req [:convex-web.command/mode
                :convex-web.command/query]
          :opt [:convex-web.command/address]))

(defmethod incoming-command :convex-web.command.mode/transaction [_]
  (s/keys :req [:convex-web.command/mode
                :convex-web.command/address
                :convex-web.command/transaction]))

(s/def :convex-web/incoming-command (s/multi-spec incoming-command :convex-web.command/mode))

(defmulti command :convex-web.command/status)

(defmethod command :default [_]
  :convex-web/incoming-command)

(defmethod command :convex-web.command.status/running [_]
  (s/merge :convex-web/incoming-command
           (s/keys :req [:convex-web.command/id
                         :convex-web.command/status])))

(defmethod command :convex-web.command.status/success [_]
  (s/merge :convex-web/incoming-command
           (s/keys :req [:convex-web.command/id
                         :convex-web.command/status
                         :convex-web.command/object])))

(defmethod command :convex-web.command.status/error [_]
  (s/merge :convex-web/incoming-command
           (s/keys :req [:convex-web.command/status
                         :convex-web.command/error]
                   :opt [:convex-web.command/id])))

(s/def :convex-web/command (s/multi-spec command :convex-web.command/status))


;; -- Accounts Explorer

(s/def :convex-web.explorer/accounts (s/coll-of :convex-web/account))


;; -- REPL

(s/def :convex-web.repl/mode :convex-web.command/mode)
(s/def :convex-web.repl/commands-by-id (s/map-of :convex-web.command/id :convex-web/command))


;; -- Comms

(s/def :comms/state map?)


;; -- Blockchain

(s/def :blockchain/blocks vector?)


;; -- Devtools

(s/def :devtools/enabled? boolean?)


;; -- Page

(s/def :page/id keyword?)
(s/def :page/title string?)
(s/def :page/component var?)
(s/def :page/initial-state any?)
(s/def :page/state-spec (fn [x]
                          (or (s/get-spec x) (s/spec? x) (fn? x))))
(s/def :page/on-push fn?)
(s/def :page/on-pop fn?)


;; -- Site Page

(s/def :site/page (s/keys :req [:page/id
                                :page/component]
                          :opt [:page/title
                                :page/initial-state
                                :page/state-spec
                                :page/on-push
                                :page/on-pop]))

(s/def :site/pages
  (s/coll-of :site/page))


;; -- Stack

(s/def :frame/uuid uuid?)
(s/def :frame/page :site/page)
(s/def :frame/state any?)

(s/def :stack/frame (s/keys :req [:frame/uuid
                                  :frame/page]
                            :opt [:frame/state]))

(s/def :site/stack
  (s/coll-of :stack/frame))


;; -- Route

(s/def :route/match #(= reitit.core.Match (type %)))
(s/def :route/state any?)


;; -- Session

(s/def :convex-web.session/accounts (s/coll-of :convex-web/account))

;; -- Site Session

(s/def :site/session (s/keys :opt [:convex-web.session/accounts]))

;; -- Site Route

(s/def :site/route
  (s/keys :opt [:route/match :route/state]))

;; -- Site Comms

(s/def :site/comms
  (s/keys :req [:comms/state]))


;; -- Site Devtools

(s/def :site/devtools
  (s/keys :opt [:devtools/enabled?]))


;; -- Site Blockchain

(s/def :site/blockchain
  (s/keys :req [:blockchain/blocks]))


;; -- Site DB

(s/def :site/app-db
  (s/keys :req [:site/pages]
          :opt [:site/comms
                :site/route
                :site/devtools
                :site/session
                :site/blockchain]))

