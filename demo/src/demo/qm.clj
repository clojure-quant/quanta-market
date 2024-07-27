(ns demo.qm
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop!]]
   [quanta.market.quote :refer [quote-manager-start]]
   [demo.accounts :refer [accounts-quote]]
   [demo.logging] ; for side effects
   ))

(def qm (quote-manager-start accounts-quote))

(comment

  (p/start-quote qm)


  ;; one quote

  (start-flow-logger!
   ".data/quotes-btc.txt"
   :quote/one
   (p/get-quote qm {:account :bybit
                    :asset "BTCUSDT"}))

  (stop! :quote/one)

  ;; multiple quotes

  (let [assets ["BTCUSDT" "ETHUSDT"]
        qsubs (map (fn [asset]
                     {:asset asset :account :bybit}) assets)
        quotes (map #(p/get-quote qm %) qsubs)
        last-quotes (apply m/latest vector quotes)]
    (start-flow-logger!
     ".data/quotes-multi.txt"
     :multi-quote
     last-quotes))


  (stop! :multi-quote)

  ;; shutdown

  (p/stop-quote qm)




;
  )

