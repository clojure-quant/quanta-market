(ns quanta.market.algo.order
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.quote.core :refer [topic-snapshot]]
   [quanta.market.precision :refer [round-asset]]))

(defn price-off-market [price side diff]
  (let [diff (/ diff 100.0 )]
  (case side
    :buy (* price (- 1.0 diff))
    :sell (* price (+ 1.0 diff)))))

(defn limit-near-market 
  "returns the limit-price 
   diff percentage better than the 
   last trade trade price 
   using feed :feed
   for order :asset :side"
  [qm {:keys [asset side feed diff]}]
  (let [quote (topic-snapshot qm {:feed feed
                                  :asset asset
                                  :topic :asset/trade})]
    (m/sp
     (let [snapshot (m/? quote)
         ; _ (println "SNAPSHOT: " snapshot)
           {:keys [price size time]} snapshot
           off-price (price-off-market price side diff)
           off-price-precision (round-asset asset off-price)]
       off-price-precision))))


(defn limit-order-near-market [qm {:keys [asset side account qty feed diff]
                                   :as order}]
  (let [limit-price-t (limit-near-market qm order)]
  (m/sp
   (let [limit-price (m/? limit-price-t)]
     {:account account
      :asset (str asset ".S")
      :side side
      :qty qty
      :ordertype :limit
      :limit limit-price}))))


(comment

  (price-off-market 60000.0 :buy 0.001)
  (price-off-market 60000.0 :sell 0.001)

;  
  )
