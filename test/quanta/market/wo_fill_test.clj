(ns quanta.market.wo-fill-test
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
                    :qty 0.5}}
           {:order {:order-id "456"
                    :asset "BTC"
                    :side :buy
                    :limit 60000.0
                    :qty 0.14}}
           {:order-update {:order-id "456"
                           :orderupdatetype :trade
                           :trade-qty 0.05
                           :trade-value 200.0}}
           {:order-update {:order-id "456"
                           :orderupdatetype :trade
                           :trade-qty 0.05
                           :trade-value 300.0}}]))

(def om (order-manager-start
         {:db nil
          :alert-logfile ".data/test-fill.txt"
          :order-orderupdate-flow order-orderupdate-flow}))

om


(def working-order-summary
  (->> (get-working-orders om)
       (map :order-status)
       (map #(select-keys % [:order-id :status :fill-qty]))))


working-order-summary
;; => ({:status :open, :fill-qty 0.0} {:status :open, :fill-qty 0.1})



(deftest working-orders-partial-fill
  (testing "partial fill"
    (is (= working-order-summary
           '({:status :open, :fill-qty 0.0} {:status :open, :fill-qty 0.1})))))
