(ns quanta.position.risk
  (:require
   [missionary.core :as m]
   [tick.core :as t]
   [quanta.market.util :refer [flow-sender start-logging mix current-value]]
   [quanta.market.protocol :as p]))

(defn start-risk-manager [portfolio]
  portfolio)

(defn can-open [port trade]
  (let [wo-op-f (m/latest
                 snapshot
                 (p/working-order-f port) ;m( p)
                 (p/open-position-f port))
        current (current-value wo-op-f)]
    current))

