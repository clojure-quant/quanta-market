(ns quanta.market.broker.bybit-quotefeed
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.task.subscription :as s]
   [quanta.market.broker.bybit.msg.lasttrade :as lt]
   [quanta.market.broker.bybit.websocket :refer [create-websocket]]))


(defrecord bybit-feed [opts websocket]
  p/quotefeed
  (socket [this]
          (:websocket this))
  (subscribe-last-trade! [this {:keys [asset]}]
     (s/subscription-start!
       (p/current-connection (p/socket this))
       :asset/trade asset))
  (unsubscribe-last-trade! [this {:keys [asset]}]
     (s/subscription-stop!
       (p/current-connection (p/socket this)) :asset/trade asset))
  (last-trade-flow [this account-asset]
    (let [flow (p/msg-in-flow (p/socket this))]
      (assert flow "missing msg-in-flow")
      (lt/last-trade-flow flow account-asset))))
 

(defmethod p/create-quotefeed :bybit
  [opts]
  (info "creating bybit : " opts)
  (let [opts (merge {:mode :main
                     :segment :spot} opts)
        websocket (create-websocket opts)]
    (bybit-feed. opts websocket)))



  