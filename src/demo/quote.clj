(ns demo.quote
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-logging]]
   [demo.tm :refer [qm]]
   [quanta.market.algo.almost-market-order :refer [get-last-trade-price]]
   ))


(def qsub {:account :bybit
           :asset "BTCUSDT"})

(start-logging ".data/quotes4.txt"
               (p/last-trade-flow qm qsub))


(m/? (p/subscribe-last-trade! qm qsub))

(m/? (p/unsubscribe-last-trade! qm qsub))


(m/? (get-last-trade-price qm :bybit "BTCUSDT")
 
 )



