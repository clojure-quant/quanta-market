(ns demo.dev.order-state
  (:require
   [missionary.core :as m]
   [quanta.market.trade.order-state :refer [get-working-orders
                                            order-manager-start]]))

(def order-orderupdate-flow
  (m/seed [{:order {:order-id "123"
                    :asset "BTC"
                    :side :buy
                    :limit 60000.0
                    :qty 0.001}}
           {:order {:order-id "456"
                    :asset "BTC"
                    :side :buy
                    :limit 60000.0
                    :qty 0.001}}]))

(def om (order-manager-start
         {:db nil
          :order-orderupdate-flow order-orderupdate-flow
          :alert-logfile ".data/test-alerts.txt"}))

(get-working-orders om)
