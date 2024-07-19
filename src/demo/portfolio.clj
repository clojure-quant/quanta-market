(ns demo.portfolio
   (:require
    [missionary.core :as m]
    [quanta.market.trade.db :as trade-db :refer [trade-db-start
                                                 trade-db-stop]]
    [quanta.market.portfolio :refer [portfolio-manager-start
                                     get-working-orders
                                     ]]
))
  

(def order-update-flow 
  (m/seed 
   [{:type :order/new 
     :order-data {:account :alex1 
                  :asset "BTC"
                  :side :buy
                  :qty 50}}
   ]))



(def pm (portfolio-manager-start {:db nil 
                                  :order-update-flow order-update-flow
                                  }))

(get-working-orders pm)