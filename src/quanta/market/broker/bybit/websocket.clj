(ns quanta.market.broker.bybit.websocket
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.util :refer [flow-sender]]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.connection :as c]
   [quanta.market.broker.bybit.task.auth :as a]
   [quanta.market.broker.bybit.pinger :as pinger]))

(defrecord bybit-websocket [opts conn ping
                            flow-sender-in flow-sender-out]
  ;
  p/connection
  (start! [this]
    (let [new-conn (c/connection-start! flow-sender-in flow-sender-out opts)]
      (reset! conn new-conn)
      ; auth
      (when-let [creds (:creds opts)]
        (info (:account-id opts) " authenticating secure account..")
        (m/? (a/authenticate! new-conn creds)))
      ; pinger
      (pinger/start-pinger new-conn ping)
      new-conn))
  (stop! [this]
    (let [cur-conn (p/current-connection this)]
      (pinger/stop-pinger ping)
      (when cur-conn
        (info (:account-id opts) "stopping connection..")
        (c/connection-stop! cur-conn)
        (reset! conn nil))
      nil))
  (current-connection [this]
    @conn)
  (msg-in-flow [this]
    (:flow flow-sender-in))
  (msg-out-flow [this]
    (:flow flow-sender-out))
  ; bybit websocket
  )
(defn create-websocket
  [opts]
  (info "creating bybit-websocket : " opts)
  (let [conn (atom nil)
        ping (atom nil)
        flow-sender-in (flow-sender)
        flow-sender-out (flow-sender)]
    (bybit-websocket. opts
                      conn
                      ping
                      flow-sender-in
                      flow-sender-out)))


