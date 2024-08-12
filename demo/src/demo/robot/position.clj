(ns demo.robot.position
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [clojure.pprint :refer [print-table]]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop!]]
   [quanta.market.robot.position.working :refer [working-position]]
   [demo.env :refer [qm pm]])
  (:import [missionary Cancelled]))

(def wp
  (working-position qm {:asset "BTCUSDT"
                        :feed :bybit
                        :qty 500
                        :entry-price 1000.0
                        :entry-date (t/instant)}))


(start-flow-logger!
 ".data/working-position.txt"
 :wp wp)

(stop! :wp)


(def positions [{:asset "BTCUSDT"
                 :feed :bybit
                 :side :long
                 :qty 500
                 :entry-price 50000.0
                 :entry-date (t/instant)}
                {:asset "BTCUSDT"
                 :feed :bybit
                 :side :short
                 :qty 500
                 :entry-price 60000.0
                 :entry-date (t/instant)}
                {:asset "ETHUSDT"
                 :feed :bybit
                 :side :long
                 :qty 500
                 :entry-price 4000.0
                 :entry-date (t/instant)}
                {:asset "ETHUSDT"
                 :feed :bybit
                 :side :short
                 :qty 500
                 :entry-price 3000.0
                 :entry-date (t/instant)}])


(defn print-positions [& positions]
  (with-out-str 
     (print-table [:asset :side
                 :exit-price
                 :ret-prct
                 :win?] positions)))
  
(def table-print-f 
   (let [flows (map #(working-position qm %) positions)]  
     (apply m/latest print-positions flows)))


(start-flow-logger!
 ".data/working-positions.txt"
 :wps table-print-f)

(stop! :wps)