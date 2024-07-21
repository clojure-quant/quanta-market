(ns quanta.market.trade.order-status
  (:require [tick.core :as t])
  )

(def update-types
  #{; open
    :order/new
    ; close
    :order/canceled
    :order/expired
    :order/cancelled
    ; trade
    :order/trade})

(def order-status
  #{; create new order
    :order/new
    :order/order-confirm
    :order/reject

    ; cancel order
    :order/cancel-req
    :order/cancel-reject
    :order/cancel-confirm
    :order/cancelled

    ; trade
    :order/fill-partial
    :order/fill

    ; expired orders
    :order/expired})

(defn open-order [order-status]
  (assoc order-status
         :status :open
         :open-date (t/inst)
         :fill-qty 0.0
         :fill-volume 0.0))

(defn close-order [order-status reason]
  (assoc order-status 
         :status :closed
         :close-reason reason
         :close-date (t/inst)))

(defn process-trade [order order-status trade-qty trade-price]
  (let [trade-volume (+ trade-qty trade-price)
        status+trade (-> order-status
                         (update :fill-qty + trade-qty)
                         (update :fill-volume + trade-volume))  
        ]
    (if (>= (:fill-qty status+trade) (:qty order))
      (close-order status+trade "fill")
      status+trade)))

(defn open? [order-status]
  (= (:status order-status) :open))

(defn update-existing-order-status [{:keys [order order-status]} 
                             {:keys [type trade-qty trade-price] :as order-update}]
   (case type
    ; close
    :order/canceled (close-order order-status "canceled")
    :order/expired (close-order order-status "expired")
        ; trade
    :order/trade (process-trade order order-status trade-qty trade-price)
    order-status))
