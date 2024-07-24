(ns demo.order-near-market
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.algo.price :refer [get-last-trade-price]]
   [quanta.market.algo.order :refer [order-with-limit-near-market]]
   [demo.qm :refer [qm]]
   [demo.tm :refer [pm]]
   ))


(m/? (get-last-trade-price qm :bybit "BTCUSDT"))


;; => {:asset "BTCUSDT", :price 68319.75, :size 9.99E-4, :time 1721609641809}

(m/? (order-with-limit-near-market qm {:asset "BTCUSDT"
                              :side :buy
                              :account :rene/test4}))
;; => {:ordertype :limit, 
;;     :qty 1.0E-4, 
;;     :account :rene/test4, 
;;     :side :buy, 
;;     :asset "BTCUSDT.S", 
;;     :limit 67998.94}

(let [order (m/? (order-with-limit-near-market qm {:asset "BTCUSDT.S"
                                          :side :buy
                                          :account :rene/test4
                                          }))]
  (m/? (p/order-create! pm order)))







