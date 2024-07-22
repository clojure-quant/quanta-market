(ns demo.quotes-multi)



(let [assets ["BTCUSDT" "ETHUSDT"]
      get-quote (fn [asset]
                  (get-quote :bybit quote-account asset))
      quotes (map get-quote assets)
      quotes-cont (map cont quotes)
      last-quotes (apply m/latest vector quotes-cont)]
  (m/?
   (m/reduce println nil last-quotes))
     ;(m/reduce print-quote nil (first quotes))
  )