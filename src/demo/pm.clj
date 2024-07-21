(ns demo.pm
  (:require
   [missionary.core :as m]
   [quanta.market.portfolio :refer [portfolio-manager-start
                                    get-working-orders]]
   [demo.tm :refer [tm]]))

(def order-update-flow
  (m/seed
   [{:type :order/new
     :order-data {:account :alex1
                  :asset "BTC"
                  :side :buy
                  :qty 50}}]))




(def pm (portfolio-manager-start {:db nil
                                  ;:order-update-flow order-update-flow
                                  :tm tm
                                  :alert-logfile ".data/alerts.txt"
                                  }))


;(get-working-orders pm)