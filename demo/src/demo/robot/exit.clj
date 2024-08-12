(ns demo.robot.exit
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [clojure.pprint :refer [print-table]]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop!]]
   [quanta.market.robot.exit.price :refer [profit-trigger]]
   [demo.env :refer [qm pm]])
  (:import [missionary Cancelled]))



(def env {:qm qm})

(def algo-opts {:asset "BTCUSDT"
                :exit [:profit-percent 1.0]})

(def position
    {:asset "BTCUSDT"
     :feed :bybit
     :side :long
     :qty 500
     :entry-price 10000.0})

(m/? (profit-trigger env algo-opts position))
;; returns :profit once the profit target is met.
;; since BTCUSDT price is above 10k it returns :profit 
;; immediately after the first quote is received.
;; => :profit


