(ns demo.spot-future
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop! mix]]
   [quanta.market.broker.bybit.quotefeed] ; side effects
   [demo.logging] ; for side effects
   ))

(def bb-quote (p/create-quotefeed {:type :bybit}))

(defn future-premium [asset]
  (let [asset-perpetual (str asset ".P")
        spot (p/trade bb-quote {:asset asset})
        perpetual (p/trade bb-quote {:asset asset-perpetual})
        premium (fn [spot perpetual]
          (let [p-spot (:price spot)
                p-perp (:price perpetual)
                abs (when (and p-spot p-perp)
                      (- p-perp p-spot))
               prct (when abs
                      (/ (* 100.0 abs) p-spot))]
            {:asset asset 
             :spot p-spot 
             :perp p-perp 
             :abs abs
             :prct prct
             }))]
    (m/latest premium spot perpetual)))


(start-flow-logger!
 ".data/spot-future-btc.txt"
 :spot-future-btc
 (future-premium "BTCUSDT"))

(stop! :spot-future-btc)


(defn future-premiums [assets]
  (apply mix (map future-premium assets)))


(start-flow-logger!
 ".data/spot-future-multiple.txt"
 :spot-future
 (future-premiums ["BTCUSDT" "ETHUSDT"]))

(stop! :spot-future)





