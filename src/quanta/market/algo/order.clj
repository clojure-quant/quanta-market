(ns quanta.trade.algo.order
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.algo.price :refer [get-last-trade-price]]))


(defn price-off-market [price side]
  (let [diff 0.001]
    (case side
      :buy (* price (- 1.0 diff))
      :sell (* price (+ 1.0 diff)))))

(get-last-trade-price qm :bybit "BTCUSDT")

(defn almost-market-order [qm {:keys [asset side account]}]
  (m/sp
   (let [{:keys [price size time]} (m/? (get-last-trade-price qm :bybit asset))
         off-price (price-off-market price side)]
     {:account account
      :asset (str asset ".S")
      :side side
      :qty 0.0001
      :ordertype :limit
      :limit off-price})))


(comment
  (price-off-market 60000.0 :buy)
  (price-off-market 60000.0 :sell)


;  
  )
