(ns demo.start
  (:require 
   [quanta.market.trade.db :refer [start-trade-db]])
  )

 (def bybit-test-creds
  (-> (System/getenv "MYVAULT")
      (str "/goldly/quanta.edn")
      slurp
      read-string
      :bybit/test))


(def accounts
  {; quote connections
   :random {:type :random}
   :bybit {:type :bybit
           :mode :main
           :segment :spot}
   :bybit-test {:type :bybit-account
                :mode :test
                :segment :trade
                :account bybit-test-creds}
     ; trade connections
   })
 
 (def db (start-trade-db "/tmp/trade-db"))
 conn





  (def this (start-account-manager demo-accounts))
  
  this
  
  (require '[missionary.core :as m])
  
  (def print-quote (fn [r q] (println q)))
  
  (m/? (m/reduce
        print-quote nil (get-quote this {:account :random
                                         :asset "BTC"})))
  
  (m/? (m/reduce
        print-quote nil (get-quote this {:account :bybit
                                         :asset "BTCUSDT"})))
