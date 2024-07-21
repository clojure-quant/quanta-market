(ns quanta.market.broker.bybit.msg.lasttrade
  (:require
   [missionary.core :as m]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.broker.bybit.topic :as topic]
   [quanta.market.util :refer [split-seq-flow]]))

(def last-trade-msg-demo
  {"type" "snapshot"
   "topic" "publicTrade.ETHUSDT"
   "ts" 1706476799818
   "data" [{"i" "2280000000184479515"
            "T" 1706476799815
            "p" "2263.77"
            "v" "0.01056"
            "S" "Buy"
            "s" "ETHUSDT"
            "BT" false}]})

(def trade-msg-multiple-trades-demo
  {"type" "snapshot"
   "topic" "publicTrade.ETHUSDT"
   "ts" 1706476982156
   "data" [{"i" "2280000000184480265",
            "T" 1706476982154,
            "p" "2262.6",
            "v" "0.19676",
            "S" "Sell",
            "s" "ETHUSDT",
            "BT" :false}
           {"i" "2280000000184480266"
            "T" 1706476982154
            "p" "2262.6"
            "v" "0.17735"
            "S" "Sell"
            "s" "ETHUSDT"
            "BT" false}
           {"i" "2280000000184480267"
            "T" 1706476982154
            "p" "2262.6"
            "v" "0.00512"
            "S" "Sell"
            "s" "ETHUSDT"
            "BT" false}]})


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
  {:order-id orderLinkId
   :cum-exec-qty cumExecQty
   :cum-exec-value cumExecValue
   :timestamp updatedTime
   :reject-reason rejectReason})

(defn parse-bybit-trade [{:keys [s p v T]}]
  {:asset s
   :price p
   :size v
   :time T})

;({:asset BTCUSDT, :price 60007.77, :size 9.9E-5}
; {:asset BTCUSDT, :price 60007.94, :size 0.003858}
; {:asset BTCUSDT, :price 60008.02, :size 0.001843})
;({:asset BTCUSDT, :price 60008.97, :size 0.0049})


(defn last-trade-flow [msg-flow {:keys [asset]}]
  (let [t (topic/topic :asset/trade [asset])]
    (warn "subscribing last-trade for asset: " asset " topic: " t)
  (m/eduction (filter (topic/only-topic t)) msg-flow)))
  

(comment 
  (topic/topic :asset/trade ["BTCUSDT"])
  
  ;
  )

