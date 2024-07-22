(ns quanta.market.algo.order
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.algo.price :refer [get-last-trade-price]]
   [quanta.market.precision :refer [round-asset]]))


(defn price-off-market [price side]
  (let [diff 0.001]
    (case side
      :buy (* price (- 1.0 diff))
      :sell (* price (+ 1.0 diff)))))

(defn order-with-limit-near-market [qm {:keys [asset side account qty]
                                        :or {qty 0.001}
                                        :as order}]
  (m/sp
   (let [{:keys [price size time]} (m/? (get-last-trade-price qm :bybit asset))
         off-price (price-off-market price side)
         off-price-precision (round-asset asset off-price)]
     {:account account
      :asset (str asset ".S")
      :side side
      :qty qty
      :ordertype :limit
      :limit off-price-precision})))


(comment
  
  (price-off-market 60000.0 :buy)
  (price-off-market 60000.0 :sell)


;  
  )
