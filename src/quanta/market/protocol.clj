(ns quanta.market.protocol)

(defprotocol manage-connection
  (start! [this])
  (stop! [this])
  )

(defprotocol connection
  (current-connection [this])
  (msg-in-flow [this])
  (msg-out-flow [this]))

(defprotocol connection-subscriber
   (subscription-start! [this conn topic]) 
   (subscription-stop! [this conn topic]) 
   (topic-view [this conn topic]))


;; QUOTE

(defprotocol subscription-topic
  (get-topic [this sub]) 
  )

(defprotocol quote
  (trade [this sub])
  (orderbook [this sub]))

(defmulti create-quotefeed
  (fn [opts]
    (:type opts)))

;; TRADE

(defprotocol trade-action
  (order-create! [this order-new])
  (order-cancel! [this order-cancel]))

(defprotocol tradeaccount
  (start-trade [this])
  (stop-trade [this])
  (msg-flow [this])
  (order-update-msg-flow [this])
  (order-update-flow  [this]))

(defmulti create-tradeaccount
  (fn [opts]
    (:type opts)))

(defprotocol portfolio
  (working-order-f [this])
  (open-position-f [this])
  (trade-f [this])
  (snapshot [this]))



