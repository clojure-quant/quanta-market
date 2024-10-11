(ns quanta.market.broker.bybit.order.cancel)

(defn order-cancel-msg [{:keys [asset order-id]}]
  {"op" "order.cancel"
   "header" {"X-BAPI-TIMESTAMP" (System/currentTimeMillis)
             "X-BAPI-RECV-WINDOW" "8000"
             "Referer" "bot-001" ; for api broker
             }
   "args" [{"category" "linear" ; product type: spot, linear, inverse, option
            "symbol" asset
            ;"orderId" order-id ; bybit order id
            "orderLinkId" order-id ; user order id
            }]})