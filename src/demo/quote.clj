(ns demo.quote
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-logging]]
   [demo.tm :refer [qm]]))


(def qsub {:account :bybit
           :asset "ETHUSDT"})

(m/? (p/subscribe-last-trade! qm qsub))



(start-logging ".data/quotes.txt"
               (p/last-trade-flow qm qsub))

