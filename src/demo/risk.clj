(ns demo.risk
  (:require
    [missionary.core :as m]
    [quanta.market.protocol :as p]
    [demo.tm :refer [pm]]
   ))


 (p/snapshot pm)
 ;; => {:working-orders
 ;;     ({:open-order
 ;;       {:account :rene/test4,
 ;;        :asset "BTCUSDT.S",
 ;;        :side :buy,
 ;;        :qty 0.002,
 ;;        :ordertype :market,
 ;;        :order-id "NfEZrKaF",
 ;;        :date-created #inst "2024-07-27T15:17:47.994-00:00"},
 ;;       :order-status {:status :open, :open-date #inst "2024-07-27T15:17:48.060-00:00", :fill-qty 0.0, :fill-value 0.0},
 ;;       :order-id "NfEZrKaF"}),
 ;;     :open-positions {}}
 




  