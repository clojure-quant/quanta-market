(ns quanta.market.protocol)

(defmulti create-account
  (fn [opts]
    (:type opts)))

(defprotocol connection
  (start! [this])
  (stop! [this])
  (current-connection [this])
  (msg-in-flow [this])
  (msg-out-flow [this]))

;; QUOTE

(defprotocol quotefeed
  (socket [this])
  (subscribe-last-trade! [this sub])
  (unsubscribe-last-trade! [this unsub])
  (last-trade-flow [this account-asset]))

(defmulti create-quotefeed
  (fn [opts]
    (:type opts)))

;; TRADE

(defprotocol tradeaccount
  (start-trade [this])
  (stop-trade [this])
  (order-create! [this order-new])
  (order-cancel! [this order-cancel])
  (order-update-msg-flow [this])
  (order-update-flow  [this]))

(defmulti create-tradeaccount
  (fn [opts]
    (:type opts)))



