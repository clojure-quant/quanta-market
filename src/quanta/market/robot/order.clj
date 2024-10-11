(ns quanta.market.robot.order
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.quote.core :refer [topic-snapshot]]
   [quanta.market.precision :refer [round-asset]]))

(defn price-off-market [price side diff]
  (let [diff (/ diff 100.0)]
    (case side
      :buy (* price (- 1.0 diff))
      :sell (* price (+ 1.0 diff)))))

(defn limit-near-market
  "returns a missionary task that returns the limit-price 
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

(defn limit-order-near-market
  "returns a missionary task that returns a limit-order
    whose limit is diff percentage better than the last 
    trade trade price received using feed :feed
    for order :asset :side"
  [qm {:keys [asset side account qty feed diff]
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

(defn place-order-near-market
  "returns a missionary task that returns a places
      a limit-order near the last trade 
      whose limit is diff percentage better than the last 
      trade trade price received using feed :feed
      for order :asset :side"
  [{:keys [qm pm]} order-feed-diff]
  (let [order-create-t (limit-order-near-market qm order-feed-diff)
        ; order-place-t  ; cannot use the let trick here. 
        ]
    (m/sp
     (let [order (m/? order-create-t)]
       (warn "will place order: " order)
       (m/? (p/order-create! pm order))))))

(comment

  (price-off-market 60000.0 :buy 0.001)
  (price-off-market 60000.0 :sell 0.001)

;  
  )
