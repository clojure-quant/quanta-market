(ns quanta.market.broker.bybit
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.util :refer [first-match flow-sender]]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.connection :as c]
   [quanta.market.broker.bybit.task.auth :as a]
   [quanta.market.broker.bybit.task.order :as o]
   [quanta.market.broker.bybit.task.subscription :as s]
   [quanta.market.broker.bybit.pinger :as pinger]
   [quanta.market.broker.bybit.msg.orderupdate :as ou]
   ))


(defrecord bybit [opts conn ping 
                  flow-sender-in flow-sender-out
                  order-update-msg-flow order-update-flow]
  ;
  p/connection
  (start! [this opts]
    (let [conn (c/connection-start! flow-sender-in flow-sender-out opts)]
      (reset! (:conn this) conn)
      ; auth
      (when-let [creds (:creds opts)]
        (info (:account-id opts) " authenticating secure account..")
        (m/? (a/authenticate! conn creds)))
      ; orderupdate subscription
      (when (= (:segment opts) :private)    
        (info (:account-id opts) "segment=private -> subscribing to execution and orderupdates..")
        ; ticketInfo does not work.
        (m/? (s/subscription-start! conn :order/execution))
        (m/? (s/subscription-start! conn :order/update))
        )
      ; pinger
      (pinger/start-pinger conn ping)
      conn))
  (stop! [this opts]
    (let [conn @(:conn this)]
      (pinger/stop-pinger (:ping this))
      (when conn
        (info (:account-id opts) "stopping connection..")
        (c/connection-stop! conn)
        (reset! (:conn this) nil))
      nil))
  (msg-in-flow [this]
     (:flow (:flow-sender-in this)))
  (msg-out-flow [this]
      (:flow (:flow-sender-out this)))
              
  ;
  p/trade
  (order-create! [this order]
    (o/order-create! @(:conn this) order))
  (order-cancel! [this order]
    (o/order-cancel! @(:conn this) order))
  (order-update-msg-flow [this] (:order-update-msg-flow this))
  (order-update-flow [this] (:order-update-flow this))

  ; p/quote
  ;(quote-stream [this]
  ;  (get-stream this))
  )


(defmethod p/create-account :bybit
  [opts]
  (info "creating bybit : " opts)
  (let [flow-sender-in (flow-sender)
        flow-sender-out (flow-sender)
        order-update-msg-flow  (ou/order-update-msg-flow (:flow flow-sender-in))
        order-update-flow (ou/order-update-flow order-update-msg-flow)
        ]
  (bybit. opts
          (atom nil)
          (atom nil)
          flow-sender-in 
          flow-sender-out
          order-update-msg-flow
          order-update-flow)))


  