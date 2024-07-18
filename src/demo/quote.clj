(ns demo.quote)




 
  
  (require '[missionary.core :as m])
  
  (def print-quote (fn [r q] (println q)))
  
  (m/? (m/reduce
        print-quote nil (get-quote this {:account :random
                                         :asset "BTC"})))
  
  (m/? (m/reduce
        print-quote nil (get-quote this {:account :bybit
                                         :asset "BTCUSDT"})))
