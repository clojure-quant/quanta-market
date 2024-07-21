(ns quanta.market.working-order-test
  (:require
   [clojure.test :refer :all]
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
          :order-orderupdate-flow order-orderupdate-flow}))

(def working-order-summary 
  (->> (get-working-orders om)
       (map :order-status)
       (map #(select-keys % [:order-id :status :fill-qty])))  
  )

working-order-summary
;; => ({:order-id "123", :status :open, :fill-qty 0.0} 
;;       {:order-id "456", :status :open, :fill-qty 0.0})


(deftest working-orders
  (testing "open-working-orders"
    (is (= working-order-summary 
           '({:order-id "123", :status :open, :fill-qty 0.0} 
            {:order-id "456", :status :open, :fill-qty 0.0})))
    
  ))
