(ns quanta.market.algo.price
  (:require
   [missionary.core :as m]
   [quanta.market.util :refer [next-value start!]]
   [quanta.market.protocol :as p]))

(defn get-last-trade-price [qm account asset]
  (m/sp (let [qsub {:account :bybit
                    :asset asset}
              flow (p/last-trade-flow qm qsub)
              _  (start! (p/subscribe-last-trade! qm qsub))
              result (m/? (m/race
                           (m/sleep 5000 :timeout)
                           (next-value flow)))]
      (start! (p/unsubscribe-last-trade! qm qsub))
      result)))





