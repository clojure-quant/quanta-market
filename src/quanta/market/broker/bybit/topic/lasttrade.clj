(ns quanta.market.broker.bybit.topic.lasttrade
  (:require
   [missionary.core :as m]
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

(defn normalize-bybit-last-trade [{:keys [s p v T BT S i]}]
  {:asset s
   :price (parse-double p)
   :size (parse-double v)
   :time T
   ; unchanged from raw topic
   :BT BT 
   :S S
   :i i
   })

(defn transform-last-trade-flow [topic-data-flow]
  ; output of this flow:
  ; {:asset BTCUSDT, :price 67662.78, :size 0.00761, :time 1722179969287, :BT false, :S Buy}
  (m/ap
   (let [{:keys [data]} (m/?> topic-data-flow)
         last-quote-update (m/?> (split-seq-flow data))]
     (when last-quote-update ; bug of split-seq-flow returns also nil.
       (normalize-bybit-last-trade last-quote-update)))))


(comment
 

  (m/? (->> (m/seed (range 10))
            (m/eduction (filter odd?) (mapcat range) (partition-all 4))
            (m/reduce conj)))

   ;(topic :asset/trade "BTCUSDT")

;
  )

