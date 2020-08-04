(ns dev
  (:require [convex-web.system :as system]
            [convex-web.peer :as peer]
            [convex-web.component :as component]
            [convex-web.convex :as convex]
            [convex-web.session :as session]
            [convex-web.account :as account]
            [convex-web.logging :as logging]
            [convex-web.web-server :as web-server]

            [clojure.test :refer [is]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.repl :refer [doc]]
            [clojure.string :as str]
            [clojure.java.io :as io]

            [com.stuartsierra.component.repl :refer [set-init reset system]]
            [aero.core :as aero]
            [datascript.core :as d]
            [nano-id.core :as nano-id]
            [org.httpkit.client :as http]
            [expound.alpha :as expound])
  (:import (convex.core Init Peer)
           (convex.core.lang Core Reader Context)
           (org.slf4j.bridge SLF4JBridgeHandler)))

;; -- Spec
(set! s/*explain-out* expound/printer)
(s/check-asserts true)
(stest/instrument)


(set-init
  (fn [_]
    (SLF4JBridgeHandler/removeHandlersForRootLogger)
    (SLF4JBridgeHandler/install)

    (component/system :dev)))

(def context (Context/createFake Init/INITIAL_STATE))

(defn ^Peer peer []
  (peer/peer (system/convex-server system)))

(defmacro send-query [& form]
  `(let [conn# (system/convex-conn system)]
     (peer/query conn# Init/HERO ~(str/join " " form))))

(defmacro execute [form]
  `(convex/execute context ~form))

(defmacro execute-query [& form]
  `(let [^String source# ~(str/join " " form)]
     (.getResult (.executeQuery (peer) (peer/wrap-do (Reader/readAll source#)) Init/HERO))))

(defn db []
  @(system/datascript-conn system))

(defn commands
  ([]
   (d/q '[:find [(pull ?e [*]) ...]
          :in $
          :where [?e :convex-web.command/id _]]
        @(system/datascript-conn system)))
  ([status]
   (filter
     (fn [command]
       (= status (:convex-web.command/status command)))
     (commands))))

(comment
  ;; -- Sessions
  (d/q '[:find [(pull ?e [*
                          {:convex-web.session/accounts
                           [:convex-web.account/address]}]) ...]
         :in $
         :where [?e :convex-web.session/id _]]
       @(system/datascript-conn system))


  (d/q '[:find (pull ?e [{:convex-web.session/accounts
                          [:convex-web.account/address]}]) .
         :in $ ?id
         :where [?e :convex-web.session/id ?id]]
       @(system/datascript-conn system) "mydbOh9wCdTcF_vLvUVHR")

  (session/find-session @(system/datascript-conn system) "iGlF3AZWw0eGuGfL_ib4-")


  (let [a (.getAccounts (.getConsensusState (peer/peer (system/convex-server system))))]
    (doseq [[k v] (convex.core.lang.RT/sequence a)]
      (print k v)))


  (str Init/HERO)
  (str Init/VILLAIN)


  (convex/consensus-point (convex/peer-order (peer)))


  ;; -- Query commands
  (commands)
  (commands :convex-web.command.status/running)
  (commands :convex-web.command.status/success)
  (commands :convex-web.command.status/error)

  ;; -- Execute

  (execute nil)
  (execute 1)
  (execute \h)
  (execute "Hello")
  (execute (map inc [1 2 3]))
  (execute x)

  ;; --


  (def status (convex/account-status (peer) "3333333333333333333333333333333333333333333333333333333333333333"))

  (convex/environment-data status)

  (convex/account-status-data *1)

  (account/find-by-address (db) "3333333333333333333333333333333333333333333333333333333333333333")


  ;; -- Session

  @web-server/session-ref

  (session/all (db))
  (session/find-session (db) "4feac0cd-cc06-4a3b-bcad-54596771356b")
  (session/find-account (db) "0a0CB41358185ACe6A4d8242F567Bd34A98E718E")

  ;; --


  Core/ENVIRONMENT

  (convex/core-metadata)
  (convex/reference)

  )