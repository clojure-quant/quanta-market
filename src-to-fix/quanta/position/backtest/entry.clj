(ns quanta.position.backtest.entry
  (:require
   [missionary.core :as m]
   [quanta.position.entry.size :refer [positionsize]]
   ;[quanta.trade.position.order :refer [Order]]
   [quanta.trade.position.exit.time :refer [get-exit-time time-trigger]]))

(defn EnterPosition [algo-opts side]
  (let [qty (positionsize algo-opts side)
        order-opts (assoc algo-opts
                          :side signal :qty qty)
        order (m/? (Order order-opts))] ; order is a task which produces an effect
    (if (filled? order)
      (order->position order)
      (error order)))



  (defn ExitPosition [algo-opts position]
    ;(M/ap (m/?> 
    )
 
