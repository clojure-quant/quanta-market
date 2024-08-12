(ns demo.robot.exit
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [clojure.pprint :refer [print-table]]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop!]]
   [quanta.market.robot.exit.price :refer [profit-trigger]]
   [quanta.market.robot.exit.signal :refer [exit-signal]]
   [demo.env :refer [qm pm]])
  (:import [missionary Cancelled]))

(def env {:qm qm})

(def algo-opts {:calendar [:crypto :m]
                :exit [:loss-percent 5.0
                       :profit-percent 10.0
                       :time 20]})

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

(def position2
  {:asset "BTCUSDT"
   :feed :bybit
   :side :short
   :qty 500
   :entry-price 100000.0})

(m/? (profit-trigger env algo-opts position2))

(def position3 {:asset "BTCUSDT"
                :feed :bybit
                :side :long
                :entry-date (t/instant)
                :entry-price 10000.0
                :qty 0.1})

(m/? (exit-signal env algo-opts position3))
 ;; => :time or :profit or :loss

