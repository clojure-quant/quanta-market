(ns demo.qm
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-logging]]
   [quanta.market.quote :refer [quote-manager-start]]
   [demo.accounts :refer [accounts-quote]]))

(def qm (quote-manager-start accounts-quote))

(comment

  (p/start-quote qm)
  (p/stop-quote qm)

  ;; get-quote 

  (def qsub {:account :bybit
             :asset "BTCUSDT"})
  
  (start-logging ".data/quotes-dump3.txt"
               (p/get-quote qm qsub))


  


;
  )

