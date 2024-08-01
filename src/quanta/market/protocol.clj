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
    "data-feeds that have a connection (websocket/socket),
     need to start start and stop subscription for a topic on
     an established connection, and need to do this again in 
     case the connection gets reconnected. 
     They also need to filter data fore a topic from the 
     connection inbound msg flow."
   (get-conn [this])
   (subscription-start! [this conn topic]) 
   (subscription-stop! [this conn topic]) 
   (topic-view [this topic]))


;; QUOTE

(defprotocol subscription-topic
  "this is the protocol that the user uses to get data for 
   suscriptions by topic-sub. It is implemented by the quote-subscriber
   which manages subscriptions for protocols that implement 
   connection-subscriber.
   get-topic gets a flow for a topic-sub
   get-feed gets the underlying feed impl, in case one needs more."
  (get-feed [this])
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
  (trade-action-msg-flow [this])
  (order-create! [this order-new])
  (order-cancel! [this order-cancel]))

(defprotocol trade-update
  (orderupdate-msg-flow [this])
  (orderupdate-flow [this]))


(defprotocol tradeaccount
  (start-trade [this])
  (stop-trade [this])
  (msg-flow [this])
  (order-update-msg-flow [this])
  (order-update-flow  [this]))

(defmulti create-tradeaccount
  "a tradeaccount must implement this method to create it.
   each quotefeed implementation must have a unique :type.
     A quotefeed must implement subscription-topic protocol."
  (fn [opts]
    (:type opts)))

(defprotocol portfolio
  (working-order-f [this])
  (open-position-f [this])
  (trade-f [this])
  (snapshot [this]))



