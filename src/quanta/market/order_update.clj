(ns quanta.market.order-update)


(def update-types
  #{; create new order
    :order/create-req
    :order/order-confirm
    :order/reject

    ; cancel order
    :order/cancel-req
    :order/cancel-reject
    :order/cancel-confirm
    :order/cancelled

    ; trade
    :order/trade-partial
    :order/trade-complete

    ; expired orders
    :order/expired
  })

(defn validate [order-update])



					
			