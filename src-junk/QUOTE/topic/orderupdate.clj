(ns quanta.market.broker.bybit.topic.orderupdate
  (:require
   [missionary.core :as m]
   ;[quanta.market.broker.bybit.topic :refer [ get-topic-data]]
   [quanta.market.util :refer [split-seq-flow]]))

(def order-update-msg-demo
  {:topic "order",
   ;:type is missing
   :id "102782796_20000_1460696232",
   :creationTime 1722551860339
   :data [{:cumExecQty "0.002000"
           :category "spot"
           :triggerBy ""
           :rejectReason "EC_NoError", :tpTriggerBy "", :tpLimitPrice "0.00",
           :smpGroup "0", :triggerDirection "0", :closeOnTrigger false,
           :stopOrderType "" :symbol "BTCUSDT", :orderType "Market",
           :marketUnit "baseCoin", :smpType "None", :reduceOnly false,
           :placeType "", :isLeverage "0", :cumExecFee "0.000002",
           :lastPriceOnCreated "62545.06", :orderStatus "Filled",
           :cumExecValue "125.53064000", :positionIdx "0", :avgPrice "62765.32",
           :smpOrderId "", :leavesQty "0.000000", :blockTradeId "",
           :feeCurrency "BTC", :leavesValue "5.81398000", :slTriggerBy "",
           :createdTime 1722551860334, :orderIv "", :updatedTime 1722551860337,
           :slLimitPrice "0.00", :side "Buy", :qty "0.002000", :cancelType "UNKNOWN",
           :stopLoss 0.00, :takeProfit 0.00, :timeInForce "IOC", :price "0",
           :orderLinkId "VJyIUqF0", :triggerPrice 0.00, :orderId 1743285788525029376}]})

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

(defn transform-orderupdate-flow [topic-data-flow]
  ; output of this flow:
  (m/ap
   (let [{:keys [data]} (m/?> topic-data-flow)
         order-update (m/?> (split-seq-flow data))]
     (if order-update ; bug of split-seq-flow returns also nil.
       (normalize-bybit-orderupdate order-update)
       (m/amb) ; this does not return anything, and therefore fixes split-seq-flow
       ))))
(comment
  (def raw-order-flow (m/seed [order-update-msg-demo]))

  (def orderupdate-flow (transform-orderupdate-flow raw-order-flow))

  (m/? (m/reduce conj orderupdate-flow))

; 
  )
