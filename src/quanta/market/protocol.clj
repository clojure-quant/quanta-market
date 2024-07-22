(ns quanta.market.protocol)


(defprotocol connection
  (start! [this opts])
  (stop! [this opts])
  (current-connection [this])
  (msg-in-flow [this])
  (msg-out-flow [this]))

(defprotocol trade
  (order-create! [this order-new])
  (order-cancel! [this order-cancel])
  (order-update-msg-flow [this])
  (order-update-flow  [this]))

(defmulti create-account
  (fn [opts]
    (:type opts)))

(defprotocol quotefeed
  (socket [this])
  (subscribe-last-trade! [this sub])
  (unsubscribe-last-trade! [this unsub])
  (last-trade-flow [this account-asset]))


(defmulti create-quotefeed
  (fn [opts]
    (:type opts)))

