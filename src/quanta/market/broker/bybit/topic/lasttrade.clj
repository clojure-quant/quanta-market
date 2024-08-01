(ns quanta.market.broker.bybit.topic.lasttrade
  (:require
   [missionary.core :as m]
   [quanta.market.util :refer [split-seq-flow]]))

(def last-trade-msg-demo
  {:type "snapshot"
   :topic "publicTrade.ETHUSDT"
   :ts 1706476799818
   :data [{:i "2280000000184479515"
            :T 1706476799815
            :p "2263.77"
            :v "0.01056"
            :S "Buy"
            :s "ETHUSDT"
            :BT false}]})

(def trade-msg-multiple-trades-demo
  {:type "snapshot"
   :topic "publicTrade.ETHUSDT"
   :ts 1706476982156
   :data [{:i "2280000000184480265",
           :T 1706476982154,
           :p "2262.6",
           :v "0.19676",
           :S "Sell",
           :s "ETHUSDT",
           :BT :false}
           {:i "2280000000184480266"
            :T 1706476982154
            :p "2262.6"
            :v "0.17735"
            :S "Sell"
            :s "ETHUSDT"
            :BT false}
           {:i "2280000000184480267"
            :T 1706476982154
            :p "2262.6"
            :v "0.00512"
            :S "Sell"
            :s "ETHUSDT"
            :BT false}]})

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
         _ (println "data : " data)
         last-quote-update (m/?> (split-seq-flow data))]
     (if last-quote-update ; bug of split-seq-flow returns also nil.
       (normalize-bybit-last-trade last-quote-update)
       (m/amb) ; this does not return anything, and therefore fixes split-seq-flow
       ))))


(comment
    
  (def topic-data-f (m/seed [last-trade-msg-demo 
                             trade-msg-multiple-trades-demo]))

  (def transformed-f (transform-last-trade-flow topic-data-f))

  (m/? (m/reduce conj [] transformed-f))
  ;; => [{:asset "ETHUSDT", :price 2263.77, :size 0.01056, :time 1706476799815, :BT false, :S "Buy", :i "2280000000184479515"}
  ;;     {:asset "ETHUSDT", :price 2262.6, :size 0.19676, :time 1706476982154, :BT :false, :S "Sell", :i "2280000000184480265"}
  ;;     {:asset "ETHUSDT", :price 2262.6, :size 0.17735, :time 1706476982154, :BT false, :S "Sell", :i "2280000000184480266"}
  ;;     {:asset "ETHUSDT", :price 2262.6, :size 0.00512, :time 1706476982154, :BT false, :S "Sell", :i "2280000000184480267"}]

  
;
  )

