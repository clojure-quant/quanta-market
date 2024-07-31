(ns demo.quote.debug
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop! mix]]
   [demo.env :refer [qm]]))

;; one quote

(start-flow-logger!
 ".data/quotes-btc6.txt"
 :quote/one
 (p/get-topic qm {:feed :bybit
                  :asset "BTCUSDT"
                  :topic :asset/trade
                  }))

(stop! :quote/one)

;; multiple quotes

;; this logs a vector of the most recent quote for 2 assets

(let [assets ["BTCUSDT" "ETHUSDT"]
      qsubs (map (fn [asset]
                   {:asset asset 
                    :feed :bybit
                    :topic :asset/trade
                    }) assets)
      quotes (map #(p/get-topic qm %) qsubs)
      last-quotes (apply m/latest vector quotes)]
  (start-flow-logger!
   ".data/quotes-multi7.txt"
   :multi-quote
   last-quotes))


(stop! :multi-quote)

;; multiple quotes

;; this mixes multiple quote feeds and logs to one file.

(let [assets ["BTCUSDT" "ETHUSDT"]
      qsubs (map (fn [asset]
                   {:asset asset
                    :feed :bybit
                    :topic :asset/trade}) assets)
      quotes (map #(p/get-topic qm %) qsubs)
      multiple-mixed (apply mix quotes)]
  (start-flow-logger!
   ".data/quotes-mixed.txt"
   :quotes-mixed
   multiple-mixed))


(stop! :quotes-mixed)
