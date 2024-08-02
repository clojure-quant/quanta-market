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
   ))


(defn conn [websocket-order]
  (info "getting connection for websocket-order..")
  (let [conn-f (p/current-connection websocket-order)]
    (current-v conn-f)))

(defn send-msg-t [websocket-order id msg]
  (let [conn-t (conn websocket-order)
        msg (assoc msg :reqId id
                   "req_id" id ; this is importan for quote-subscriptions.
                   )]
    (info "sending msg: " msg)
    (m/sp
     (m/? (m/sleep 50))
     (c/send-msg! (m/? conn-t) msg))))

(defn get-result-t [websocket-order id]
  (info "awaiting result req-id: " id )
  (let [order-in-f (p/msg-in-flow websocket-order)
        p-reqId (fn [{:keys [reqId req_id]}]
                  (debug "target-id: " id "reqId: " reqId "req_id: " req_id)
                  (or (= id reqId) (= id req_id)))]
    (first-match p-reqId order-in-f)))


(defn get-result [result-fn]
  (fn [send-result reply]
    (let [reply2 (result-fn reply)]
      (info "send-result-raw: " send-result
            "reply-raw: " reply
            "reply2: " reply2)
       reply2)))
  

(defn rpc-req! [websocket-order msg result-fn]
  (let [req-id (nano-id 8)]
    (debug "making rpc request:  " msg)
    (let [r (m/join (get-result result-fn)
                    (send-msg-t websocket-order req-id msg)
                    (get-result-t websocket-order req-id))]
      (m/race r
              (m/sleep 5000 {:error "request timeout after 5 seconds"
                             :msg msg})))))


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
        websocket-order (create-websocket2 (merge opts {:segment :trade}))]
    (bybit-trade-action. opts websocket-order flow-sender)))

