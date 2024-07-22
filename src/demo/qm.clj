(ns demo.qm
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-logging]]
   ;[quanta.market.algo.price :refer [get-last-trade-price]]
   [quanta.market.quote :refer [quote-manager-start]]
   [demo.accounts :refer [accounts-quote]]))

(def qm (quote-manager-start accounts-quote))

(comment

  (p/start-quote qm)
  (p/stop-quote qm)


  (def qsub {:account :bybit
             :asset "BTCUSDT"})

  (start-logging ".data/quotes-dump.txt"
                 (p/last-trade-flow qm qsub))



  (m/? (p/subscribe-last-trade! qm qsub))

  (m/? (p/unsubscribe-last-trade! qm qsub))


; (m/? (get-last-trade-price qm2 :bybit "BTCUSDT"))


;
  )

