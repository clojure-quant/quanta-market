(ns quanta.market.wo-fill-test
  (:require
   [clojure.test :refer :all]
   [missionary.core :as m]
   [quanta.market.trade.order :refer [order-change-flow
                                      working-orders-flow
                                      current-working-orders]]
   ))

(def order-orderupdate-flow
  (m/seed [{:order-id "456"
            :order {:order-id "456"
                    :asset "BTC"
                    :side :buy
                    :limit 60000.0
                    :qty 0.14}}
           {:order-id "456"
            :broker-order-status {:order-id "456"
                                  :status :open
                                  :fill-qty 0.05
                                  :fill-value 200.0
                                   }}
           {:order-id "456"
            :broker-order-status {:order-id "456"
                                  :status :open
                                  :fill-qty 0.10
                                  :fill-value 500.0
                                  }}]))


;(m/? (m/reduce conj {} (order-change-flow order-orderupdate-flow)))
;order-changes 


(defn simplify [{:keys [order-id open-order order-status]}]
  (-> order-status
      (assoc :order-id order-id)
      (dissoc :open-date)))

(def working-order-summary
  (->> (current-working-orders order-orderupdate-flow)
       (map simplify)))

working-order-summary
;; => ({:status :open, :fill-qty 0.1, :fill-value 500.0, :order-id "456"})

(deftest working-orders-partial-fill
  (testing "partial fill"
    (is (= working-order-summary
           '({:status :open, :fill-qty 0.1, :fill-value 500.0, :order-id "456"})))))
