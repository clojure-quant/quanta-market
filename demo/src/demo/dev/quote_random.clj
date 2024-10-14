(ns demo.dev.quote-random
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [demo.logging] ; for side effects
   [clojure.pprint :refer [print-table]]))

(def f (p/create-quotefeed {:type :random}))

(defn print-quotes [& quotes]
  (print-table [:asset :last :date] quotes))

(let [assets ["BTC" "ETH" "EURUSD" "QQQ" "EURUSD"]
      topics (map (fn [a] {:asset a}) assets)
      quotes (map #(p/get-topic f %) topics)]
  (m/? (m/reduce (constantly nil)
                 (apply m/latest print-quotes quotes))))

; prints entire quote-table whenever one of the quotes updates.
; note that EURUSD is subscribed twice and has the same value.
