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
   (get-conn [this])
   (subscription-start! [this conn topic]) 
   (subscription-stop! [this conn topic]) 
   (topic-view [this topic]))


;; QUOTE

(defprotocol subscription-topic
  (get-topic [this sub]))


(defmulti create-quotefeed
  "a quotefeed must implement this method to create it.
   each quotefeed implementation must have a unique :type.
   A quotefeed must implement subscription-topic protocol."
  (fn [opts]
    (:type opts)))

(defprotocol quote
  (trade [this sub])
  (orderbook [this sub]))


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



