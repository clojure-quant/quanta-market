(ns demo.quote.spot-future
  (:require
   [clojure.pprint :refer [print-table]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop! mix wrap-blk current-v first-match]]
   [demo.env :refer [qm]]
   [demo.quote.universe :refer [asset-symbols-both]]))


(defn future-premium-f [asset]
  (let [asset-perpetual (str asset ".P")
        spot (p/trade qm {:feed :bybit
                          :asset asset})
        perpetual (p/trade qm {:feed :bybit
                               :asset asset-perpetual})
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
                     :prct prct}))]
    (m/latest premium spot perpetual)))


(start-flow-logger!
 ".data/spot-future-btc1.txt"
 :spot-future-btc
 (future-premium-f "BTCUSDT"))

(stop! :spot-future-btc)


(defn future-premiums [assets]
  (apply mix (map future-premium-f assets)))


(start-flow-logger!
 ".data/spot-future-multiple9.txt"
 :spot-future
 (future-premiums ["BTCUSDT" "ETHUSDT"]))

; started 14:06

(stop! :spot-future)


(defn valid-spread [flow]
    ; same as current-v .. need to verify.
  (first-match #(not (nil? (:prct %))) flow))

(defn spread-t [asset]
  (let [spread-f (future-premium-f asset)
        spread-result-t (valid-spread spread-f)
        spread-result-blk-t (wrap-blk spread-result-t)]
    (m/race spread-result-blk-t
            (m/sleep 60000 {:asset asset :error :timeout}))))

(m/? (spread-t "BTCUSDT"))


(defn premium-list [assets]
  (let [tasks (map spread-t assets)
        spreads (m/? (apply m/join vector tasks))]
    spreads))

(premium-list ["BTCUSDT" "ETHUSDT"])
; [{:asset "BTCUSDT", :spot 65701.5, :perp 65674.6, :abs -26.89999999999418, :prct -0.04094274864347721}
;  {:asset "ETHUSDT", :spot 3276.81, :perp 3275.04, :abs -1.7699999999999818, :prct -0.0540159484376568}]

(def spreads
  (premium-list (take 10 asset-symbols-both)))


spreads

(def spreads-all
  (premium-list asset-symbols-both))

(defn error? [{:keys [error]}]
  error)

(defn spread-above [min-prct]
  (let [neg-min-prct (- 0.0 min-prct)]
    (fn [{:keys [prct]}]
      (or (> prct min-prct)
          (< prct neg-min-prct)))))


(->> spreads-all
     (filter error?)
     count)

(->> spreads-all
     (remove error?)
     print-table)

(->> spreads-all
     (remove error?)
     (filter (spread-above 0.3))
     print-table)

;|     :asset |    :spot |    :perp |                   :abs |                :prct |
;|------------+----------+----------+------------------------+----------------------|
;|  SWEATUSDT | 0.006456 | 0.006432 | -2.3999999999999716E-5 | -0.37174721189590637 |
;|    FLRUSDT |   0.0173 |  0.01724 | -6.0000000000001025E-5 | -0.34682080924856085 |
;|   VELOUSDT | 0.011331 | 0.011294 | -3.7000000000000574E-5 |   -0.326537816609307 |
;|    MONUSDT |  0.20924 |  0.20771 | -0.0015300000000000036 |  -0.7312177403938078 |
;| UXLINKUSDT |   0.1786 |   0.1778 |  -7.999999999999952E-4 | -0.44792833146696254 |
;|   BOBAUSDT |  0.23358 |  0.23287 |  -7.100000000000162E-4 | -0.30396438051203706 |
;|   SAFEUSDT |    1.033 |   1.0291 | -0.0039000000000000146 | -0.37754114230397046 |
