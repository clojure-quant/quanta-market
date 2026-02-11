(ns demo.asset.bybit-overview
  (:require
   [quanta.market.asset.load :as l]))

(l/load-assets "bybit-linear")

(->> (l/load-lists)
     count)

(->> (l/load-lists)
     (group-by :category)
     keys)
;; => (:forex :future :etf :crypto-spot :crypto-future)

(->> (l/load-lists)
     (group-by :category)
     (map (fn [[k v]]
            [k (count v)]))
     (into {}))
;; => {:forex 1078, :future 82, :etf 5570, :crypto-spot 621, :crypto-future 446}

