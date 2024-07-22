(ns quanta.market.broker.bybit.tradeaccount
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.task.order :as o]
   [quanta.market.broker.bybit.msg.orderupdate :as ou]
   [quanta.market.broker.bybit.websocket :refer [create-websocket]]))

(defrecord bybit-trade [opts websocket-order websocket-orderupdate]
  p/tradeaccount
  (start-trade [this]
     (info "connecting bybit-trade websockets ")
     (p/start! websocket-order)
     (p/start! websocket-orderupdate))
  (stop-trade [this]
     (p/stop! websocket-order)
     (p/stop! websocket-orderupdate))
  (order-create! [this order]
    (o/order-create! (p/current-connection websocket-order) order))
  (order-cancel! [this order]
    (o/order-cancel! (p/current-connection websocket-order) order))
  (order-update-msg-flow [this] 
    (ou/order-update-msg-flow (p/msg-in-flow websocket-orderupdate)))
  (order-update-flow [this] 
    (-> (p/msg-in-flow websocket-orderupdate)
        (ou/order-update-msg-flow)
        (ou/order-update-flow))))

(defmethod p/create-tradeaccount :bybit
  [{:keys [creds mode] :as opts}]
  (assert creds "bybit tradefeed needs :creds")
  (assert mode "bybit tradefeed needs :mode (:test :main)")
  (info "creating bybit tradefeed: " opts)
  (let [opts-order (merge opts {:segment :private} opts)
        opts-orderupdate (merge opts {:segment :trade} opts)
        websocket-order (create-websocket opts-order)
        websocket-orderupdate (create-websocket opts-orderupdate)]
    (bybit-trade. opts websocket-order websocket-orderupdate)))


