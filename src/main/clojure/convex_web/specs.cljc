(ns convex-web.specs
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]))


(s/def :convex-web/non-empty-string (s/and string? (complement str/blank?)))

(s/def :convex-web/blob-string (s/and :convex-web/non-empty-string #(some-> % (str/starts-with? "0x"))))

(s/def :convex-web/address
  (s/or
    :integer nat-int?
    :string-prefixed (s/and :convex-web/non-empty-string #(some-> % (str/starts-with? "#")))
    :string :convex-web/non-empty-string))

(s/def :convex-web/sig (s/and :convex-web/non-empty-string #(= 128 (count %))))

(s/def :convex-web/checksum-hex (s/and :convex-web/non-empty-string #(= 64 (count %))))

(s/def :convex-web/amount nat-int?)

(s/def :convex-web/sequence int?)

(s/def :convex-web/language #{:convex-lisp})

(s/def :convex-web/system map?)


;; -- Config Secrets

(s/def :config.secrets/key-store-passphrase :convex-web/non-empty-string)

(s/def :config.secrets/key-passphrase :convex-web/non-empty-string)

(s/def :config/secrets 
  (s/keys :req-un [:config.secrets/key-store-passphrase
                   :config.secrets/key-passphrase]))


;; -- Config Datalevin

(s/def :config.datalevin/dir :convex-web/non-empty-string)

(s/def :config/datalevin
  (s/keys :req-un [:config.datalevin/dir]))


;; -- Config Peer

(s/def :config.peer/hostname :convex-web/non-empty-string)

(s/def :config.peer/port nat-int?)

(s/def :config.peer/key-store :convex-web/non-empty-string)

(s/def :config.peer/key-passphrase :convex-web/non-empty-string)

(s/def :config.peer/etch-store-temp? boolean?)

(s/def :config.peer/etch-store-temp-prefix :convex-web/non-empty-string)

(s/def :config/peer
  (s/keys 
    :req-un [:config.peer/hostname
             :config.peer/key-store
             :config.peer/key-passphrase]
    :opt-un [:config.peer/port
             :config.peer/etch-store-temp?
             :config.peer/etch-store-temp-prefix]))


;; -- Config Web Server

(s/def :config.web-server/port nat-int?)

(s/def :config/web-server
  (s/keys :req-un [:config.web-server/port]))


;; -- Config

(s/def :convex-web/config 
  (s/keys :req-un [:config/secrets
                   :config/datalevin
                   :config/peer
                   :config/web-server]))


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
(s/def :ajax/statuses (s/or :pending :ajax/pending-status
                            :success :ajax/success-status
                            :error :ajax/error-status))


;; -- Faucet

(s/def :convex-web.faucet/target :convex-web/address)
(s/def :convex-web.faucet/amount :convex-web/amount)

(s/def :convex-web/faucet (s/keys :req [:convex-web.faucet/target
                                        :convex-web.faucet/amount]))


;; -- Key Pair

(s/def :convex-web.key-pair/account-key :convex-web/non-empty-string)
(s/def :convex-web.key-pair/private-key :convex-web/non-empty-string)

(s/def :convex-web/key-pair (s/keys :req [:convex-web.key-pair/account-key
                                          :convex-web.key-pair/private-key]))

(s/def :convex-web/key-pair-opt (s/keys :opt [:convex-web.key-pair/account-key
                                              :convex-web.key-pair/private-key]))

;; -- Account Status

(s/def :convex-web.account-status/sequence :convex-web/sequence)
(s/def :convex-web.account-status/balance nat-int?)

(s/def :convex-web/account-status (s/keys :req [:convex-web.account-status/sequence
                                                :convex-web.account-status/balance]))


;; -- Account

(s/def :convex-web.account/status :convex-web/account-status)
(s/def :convex-web.account/address :convex-web/address)
(s/def :convex-web.account/key-pair :convex-web/key-pair-opt)
(s/def :convex-web.account/registry map?)

(s/def :convex-web/account (s/keys :req [:convex-web.account/address]
                                   :opt [:convex-web.account/status
                                         :convex-web.account/key-pair
                                         :convex-web.account/registry]))

(s/def :convex-web/signer
  (s/keys :req [:convex-web.account/address
                :convex-web.account/key-pair]))

(s/def :convex-web/accounts (s/coll-of :convex-web/account))


;; --- State

(s/def :convex-web.state/accounts-count nat-int?)

(s/def :convex-web/state (s/keys :req [:convex-web.state/accounts-count]))


;; -- Syntax

(s/def :convex-web.syntax/source string?)
(s/def :convex-web.syntax/value any?)
(s/def :convex-web.syntax/meta map?)

(s/def :convex-web/syntax (s/keys :req [:convex-web.syntax/value]
                                  :opt [:convex-web.syntax/source
                                        :convex-web.syntax/meta]))

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


;; -- Result

(s/def :convex-web.result/id nat-int?)
(s/def :convex-web.result/value any?)
(s/def :convex-web.result/type string?)
(s/def :convex-web.result/error-code keyword?)

(s/def :convex-web/result
  (s/keys :req [:convex-web.result/id
                :convex-web.result/value
                :convex-web.result/type]
          :opt [:convex-web.result/error-code]))

;; -- Query

(s/def :convex-web.query/source :convex-web/non-empty-string)
(s/def :convex-web.query/language :convex-web/language)

(s/def :convex-web/query
  (s/keys :req [:convex-web.query/source
                :convex-web.query/language]))

;; -- Transaction

(s/def :convex-web.transaction/type #{:convex-web.transaction.type/invoke
                                      :convex-web.transaction.type/transfer})
(s/def :convex-web.transaction/target :convex-web/address)
(s/def :convex-web.transaction/amount :convex-web/amount)
(s/def :convex-web.transaction/source :convex-web/non-empty-string)
(s/def :convex-web.transaction/language :convex-web/language)
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
                :convex-web.transaction/source
                :convex-web.transaction/language]
          :opt [:convex-web.transaction/sequence]))

(s/def :convex-web/transaction (s/multi-spec transaction :convex-web.transaction/type))

(s/def :convex-web/transaction-block-child (s/merge :convex-web/transaction (s/keys :req [:convex-web.transaction/index])))


;; -- Block

(s/def :convex-web.block/index nat-int?)
(s/def :convex-web.block/peer :convex-web/checksum-hex)
(s/def :convex-web.block/timestamp pos-int?)
(s/def :convex-web.block/transactions (s/coll-of :convex-web/signed-data :min-count 1))

(s/def :convex-web/block (s/keys :req [:convex-web.block/index]
                                 :opt [:convex-web.block/timestamp
                                       :convex-web.block/peer
                                       :convex-web.block/transactions]))

(s/def :convex-web/blocks (s/coll-of :convex-web/block))

;; -- Command

(s/def :convex-web.command/mode #{:convex-web.command.mode/query
                                  :convex-web.command.mode/transaction})

(s/def :convex-web.command/status #{:convex-web.command.status/running
                                    :convex-web.command.status/error
                                    :convex-web.command.status/success})

(s/def :convex-web.command/id uuid?)
(s/def :convex-web.command/timestamp nat-int?)
(s/def :convex-web.command/address :convex-web/address)

(s/def :convex-web.command/signer
  (s/or
    :signer :convex-web/signer
    :account :convex-web/account))

(s/def :convex-web.command/transaction :convex-web/transaction)
(s/def :convex-web.command/query :convex-web/query)
(s/def :convex-web.command/object any?)
(s/def :convex-web.command/error any?)

(defmulti incoming-command :convex-web.command/mode)

(defmethod incoming-command :convex-web.command.mode/query [_]
  (s/keys 
    :req [:convex-web.command/id
          :convex-web.command/timestamp
          :convex-web.command/mode
          :convex-web.command/query]
    :opt [:convex-web.command/status
          :convex-web.command/signer]))

(defmethod incoming-command :convex-web.command.mode/transaction [_]
  (s/keys 
    :req [:convex-web.command/id
          :convex-web.command/timestamp
          :convex-web.command/mode
          :convex-web.command/signer
          :convex-web.command/transaction]
    :opt [:convex-web.command/status]))

(s/def :convex-web/incoming-command (s/multi-spec incoming-command :convex-web.command/mode))

(defmulti command :convex-web.command/status)

(defmethod command :default [_]
  :convex-web/incoming-command)

(defmethod command :convex-web.command.status/running [_]
  (s/merge :convex-web/incoming-command
           (s/keys :req [:convex-web.command/status])))

(defmethod command :convex-web.command.status/success [_]
  (s/merge :convex-web/incoming-command
           (s/keys :req [:convex-web.command/status]
                   :opt [:convex-web.command/object])))

(defmethod command :convex-web.command.status/error [_]
  (s/merge :convex-web/incoming-command
           (s/keys :req [:convex-web.command/status
                         :convex-web.command/error]
                   :opt [:convex-web.command/id])))

(s/def :convex-web/command (s/multi-spec command :convex-web.command/status))


;; -- Accounts Explorer

(s/def :convex-web.explorer/accounts (s/coll-of :convex-web/account))


;; -- REPL

(s/def :convex-web.repl/language :convex-web/language)
(s/def :convex-web.repl/mode :convex-web.command/mode)
(s/def :convex-web.repl/commands (s/coll-of :convex-web/command))


;; -- Devtools

(s/def :devtools/enabled? boolean?)


;; -- Page

(s/def :page-style/title-size #{:large :small})

(s/def :page/id keyword?)
(s/def :page/title string?)
(s/def :page/description string?)
(s/def :page/template #{:developer :marketing})
(s/def :page/style (s/keys :opt [:page-style/title-size]))
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
                                :page/description
                                :page/template
                                :page/style
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

(s/def :route/match some?)
(s/def :route/state any?)


;; -- Session

(s/def :convex-web.session/accounts (s/coll-of :convex-web/account))

(s/def :convex-web.session/state map?)

;; -- Site Session

(s/def :site/session
  (s/keys :opt [:convex-web.session/accounts
                :convex-web.session/state]))

;; -- Site Route

(s/def :site/route
  (s/keys :opt [:route/match :route/state]))


;; -- Site Devtools

(s/def :site/devtools
  (s/keys :opt [:devtools/enabled?]))


;; -- Site DB

(s/def :site/app-db
  (s/keys :req [:site/pages]
          :opt [:site/route
                :site/devtools
                :site/session]))

