(ns quanta.market.broker.bybit.websocket2
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.util :refer [flow-sender]]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.connection2 :as c]
   [quanta.market.broker.bybit.task.auth :as a]
   [quanta.market.broker.bybit.pinger :as pinger]))


(defn connect! [flow-sender-in flow-sender-out opts]
  (let [c (c/connection-start! flow-sender-in flow-sender-out opts)]
    (info "bybit websocket2 got a new connection: " c)
    #_(when-let [creds (:creds opts)]
        (info (:account-id opts) " authenticating secure account..")
        (m/? (a/authenticate! new-conn creds)))
                ; pinger
                ;(pinger/start-pinger new-conn ping)
    c))

(defn disconnected? [conn]
  (let [sc (:stream-consumer conn)]
    @sc))

(defn disconnected-t [conn]
  (m/sp (let [sc (:stream-consumer conn)]
          @sc)))

(defrecord bybit-websocket2 [opts flow-sender-in flow-sender-out]
  p/connection
  (current-connection [this]
    (m/ap
     (loop [new-conn (connect! flow-sender-in flow-sender-out opts)]
       (m/amb new-conn
              (recur (do (when new-conn
                           (info "waiting for connection2 to be dropped..")
                           (when-let [sc (:stream-consumer new-conn)]
                             (info "waiting for stream-consumer2 to return.")
                             (m/? (m/via m/blk @sc))
                             (m/? (m/sleep 5000))))
                         (connect! flow-sender-in flow-sender-out opts)))))))
  (msg-in-flow [this]
    (info "returning :flow flow-sender-in:  " flow-sender-in)
    (:flow flow-sender-in))
  (msg-out-flow [this]
    (:flow flow-sender-out))
  ; bybit websocket
  )




(defn create-websocket2
  [opts]
  (info "creating bybit-websocket : " opts)
  (let [flow-sender-in (flow-sender)
        flow-sender-out (flow-sender)]
    (bybit-websocket2. opts
                       flow-sender-in
                       flow-sender-out)))




