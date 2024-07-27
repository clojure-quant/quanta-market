(ns quanta.market.broker.bybit.msg.orderupdate
  (:require
   [missionary.core :as m]
   [quanta.market.broker.bybit.topic :refer [only-topic get-topic-data]]
   [quanta.market.util :refer [split-seq-flow]]))

(defn normalize-orderstatus [orderStatus rejectReason]
  ;open status
  (case orderStatus
    ; open
    "New" [:open nil] ;  order has been placed successfully
    "PartiallyFilled" [:open nil];
    "Untriggered" [:open nil] ; Conditional orders are created
    ; closed
    "Rejected"  [:closed (str "Rejected: " rejectReason)]
    "PartiallyFilledCanceled" [:closed "PartialFill"]
    "Filled" [:closed "Filled"]
    "Cancelled"  [:closed "Cancelled"]
    "Triggered" [:closed "Triggered"]
    "Deactivated" [:closed "Deactivated"]))

(defn normalize-bybit-orderupdate [{:keys [orderId
                                           orderLinkId
                                           orderStatus ;  "New"
                                           avgPrice
                                           cumExecQty ; 
                                           cumExecValue ; "0.00000000",
                                           updatedTime ; "1721421749149"
                                           ; leavesQty ; "0.000100"
                                           ;leavesValue "0.10000000",
                                           rejectReason]}]
  (let [[orderstatus close-reason]  (normalize-orderstatus orderStatus rejectReason)
        orderupdate {:order-id orderLinkId
                     :broker-order-status {:timestamp updatedTime
                                           :order-id orderLinkId
                                           :broker-order-id orderId
                                           :status orderstatus
                                           :fill-qty (parse-double cumExecQty)
                                           :fill-value (parse-double cumExecValue)}}]
    (if close-reason
      (assoc orderupdate :close-reason close-reason)
      orderupdate)))

(defn order-id [msg]
  (get-in msg [:data]))

(defn order-update-msg-flow [msg-flow]
  (m/eduction (filter (only-topic "order")) msg-flow))

(defn order-update-flow [raw-order-flow]
  (m/ap
   (let [msg (m/?> raw-order-flow)
         data (get-topic-data msg)
         bybit-order-update (m/?> (split-seq-flow data))]
     (when bybit-order-update ; bug of split-seq-flow returns also nil.
       (normalize-bybit-orderupdate bybit-order-update)))))

(comment
  (def raw-order-flow (m/seed [{:data [1 2 3]}
                               {:data [4 5]}]))

  (def order-flow (order-update-flow raw-order-flow))

  (m/? (m/reduce conj order-flow))

;(topic :order/update)

 ; 
  )
