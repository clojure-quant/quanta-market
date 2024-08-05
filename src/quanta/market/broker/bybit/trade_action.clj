(ns quanta.market.broker.bybit.trade-action
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [nano-id.core :refer [nano-id]]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [mix current-v first-match flow-sender]]
   [quanta.market.broker.bybit.websocket2 :refer [create-websocket2]]
   [quanta.market.broker.bybit.connection :as c]
   [quanta.market.broker.bybit.order.create :refer [order-create-msg]]
   [quanta.market.broker.bybit.order.create-response :refer [post-process-order]]
   [quanta.market.broker.bybit.order.cancel :refer [order-cancel-msg]]
   [quanta.market.broker.bybit.rpc :refer [rpc-req!]]
   ))



(defrecord bybit-trade-action [opts websocket-order flow-sender]
  p/trade-action
  (trade-action-flow [this]
    (:flow flow-sender))
  (trade-action-msg-flow [this]
    (let [order-in (p/msg-in-flow websocket-order)
          order-out (p/msg-out-flow websocket-order)]
      (assert order-in "order-in-msg flow nil")
      (assert order-out "order-in-msg flow nil")
      (mix order-in order-out)))
  (order-create! [this order]
    (let [send (:send flow-sender)
          order-msg (order-create-msg order)
          response-parser (post-process-order order)]
      (info "creating order: " order-msg)
      (send order)
      (rpc-req! websocket-order order-msg response-parser)))
  (order-cancel! [this order]
    (let [send (:send flow-sender)
          order-msg (order-cancel-msg order)]
      (send order)
      (rpc-req! websocket-order order-msg identity))
; 
    ))




(defn create-trade-action
  [{:keys [creds mode] :as opts}]
  (assert creds "bybit tradefeed needs :creds")
  (assert mode "bybit tradefeed needs :mode (:test :main)")
  (info "creating bybit tradefeed: " opts)
  (let [flow-sender (flow-sender)
        websocket-order (create-websocket2 (merge opts {:segment :trade})
                                           [:bybit :order (:account opts)]
                                           )]
    (bybit-trade-action. opts websocket-order flow-sender)))

