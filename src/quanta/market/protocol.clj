(ns quanta.market.protocol)


(defprotocol connection
  (start! [this opts])
  (stop! [this opts]))

(defprotocol trade
  (order-create! [this order-new])
  (order-cancel! [this order-cancel])
  ;(order-status-flow [this])
  )

(defmulti create-account 
  (fn [opts]
    (:type opts)))