(ns demo.quote.spot-future
  (:require
   [clojure.pprint :refer [print-table]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop! mix wrap-blk first-match]]
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
 ".data/spot-future-btc7.txt"
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

; this can take up to 60 seconds.
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

;|    :asset |    :spot |    :perp |                   :abs |                :prct |
;|-----------+----------+----------+------------------------+----------------------|
;|   XEMUSDT |  0.02167 |  0.02152 | -1.4999999999999736E-4 |  -0.6922011998154008 |
;|   JSTUSDT |   0.0254 |  0.02527 | -1.2999999999999817E-4 |  -0.5118110236220401 |
;| SWEATUSDT | 0.005781 |  0.00576 | -2.0999999999999318E-5 | -0.36325895173844175 |
;|  ETHWUSDT |   1.8046 |    1.798 |  -0.006599999999999939 | -0.36573201817576967 |
;|  ZETAUSDT |   0.4136 |   0.4121 | -0.0015000000000000013 |  -0.3626692456479694 |
;|   SCAUSDT |   0.2491 |    0.246 | -0.0030999999999999917 |  -1.2444801284624616 |
;|   MONUSDT |  0.16998 |  0.16945 |  -5.300000000000027E-4 | -0.31180138839863675 |
;| TAIKOUSDT |   1.7381 |   1.7326 |   -0.00550000000000006 |  -0.3164374892123618 |
;| BLASTUSDT | 0.009172 | 0.009133 | -3.8999999999999105E-5 | -0.42520715220234523 |