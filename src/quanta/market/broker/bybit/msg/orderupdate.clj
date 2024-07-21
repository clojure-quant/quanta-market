(ns quanta.market.broker.bybit.msg.orderupdate
  (:require
   [missionary.core :as m]
   [quanta.market.broker.bybit.msg.topic :refer [only-topic get-topic-data]]
   [quanta.market.util :refer [split-seq-flow]]))


(def example-rejected
  {:orderId "1733821188532430848"
   :orderLinkId "1733821188532430849"
   :rejectReason "EC_PostOnlyWillTakeLiquidity"
   :orderStatus "Rejected"
   :cumExecValue "0.00000000"
   :cumExecQty "0.000000"
   :leavesValue "6.87500000"
   :leavesQty "0.000100"

   , :category "spot", :triggerBy "",
   , :tpTriggerBy "",
   :tpLimitPrice "0.00", :smpGroup 0, :triggerDirection 0, :closeOnTrigger false,
   :stopOrderType "", :symbol "BTCUSDT", :orderType "Limit", :marketUnit "",
   :smpType "None", :reduceOnly false, :placeType "", :isLeverage "0",
   :cumExecFee "0", :lastPriceOnCreated "65485.47",,
   , :positionIdx 0, :avgPrice "",
   :smpOrderId "", , :blockTradeId "",
   :feeCurrency "", , :slTriggerBy "",
   :createdTime "1721423592093", :orderIv "",
   :updatedTime "1721423592096", :slLimitPrice "0.00",
   :side "Buy", :qty "0.000100", :cancelType "UNKNOWN",
   :stopLoss "0.00", :takeProfit "0.00", :timeInForce "PostOnly",
   :price "68750.00", ,
   :triggerPrice "0.00"})


(defn normalize-bybit-orderupdate [{:keys [orderId
                                           orderLinkId
                                           orderStatus ;  "New"
                                           avgPrice
                                           cumExecQty ; 
                                           cumExecValue ; "0.00000000",
                                           updatedTime ; "1721421749149"
                                           ; leavesQty ; "0.000100"
                                           ;leavesValue "0.10000000",
                                           rejectReason
                                           ]}]
  {:order-id orderLinkId
   :status orderStatus
   :cum-exec-qty cumExecQty
   :cum-exec-value cumExecValue
   :timestamp updatedTime
   :reject-reason rejectReason
   })


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
       (normalize-bybit-orderupdate bybit-order-update))
     )))

(comment
  (def raw-order-flow (m/seed [{:data [1 2 3]}
                               {:data [4 5]}
                               ]))
  

  (def order-flow (order-update-flow raw-order-flow))

  (m/? (m/reduce conj order-flow))



 ; 
  )
  