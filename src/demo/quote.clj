(ns demo.quote
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-logging]]
   ;[quanta.market.algo.price :refer [get-last-trade-price]]
   [quanta.market.quote :refer [quote-manager-start
                                start-all-feeds
                                stop-all-feeds
                                ]]
   ))

(def accounts
  {;:random {:type :random}
   :bybit {:type :bybit
           :mode :main
           :segment :spot}})


(def qm2 (quote-manager-start accounts))

qm2

(start-all-feeds qm2)

(stop-all-feeds qm2)



qm2

(def qsub {:account :bybit
           :asset "BTCUSDT"})

(start-logging ".data/quotes5.txt"
               (p/last-trade-flow qm2 qsub))



(m/? (p/subscribe-last-trade! qm2 qsub))

(m/? (p/unsubscribe-last-trade! qm2 qsub))


; (m/? (get-last-trade-price qm2 :bybit "BTCUSDT"))



