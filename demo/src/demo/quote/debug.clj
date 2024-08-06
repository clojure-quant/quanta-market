(ns demo.quote.debug
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop! mix]]
   [demo.env :refer [qm]]))

;; one quote


(defn add-timestamp [f]
  (m/ap  (let [msg (m/?> f)]
           (assoc msg :timestamp (t/instant)))))

(start-flow-logger!
 ".data/quotes-eth2.txt"
 :quote/one
 (add-timestamp (p/get-topic qm {:feed :bybit
                                 :asset "ETHUSDT"
                                 :topic :asset/trade})))


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
