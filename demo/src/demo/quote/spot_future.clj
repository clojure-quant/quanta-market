(ns demo.quote.spot-future
  (:require
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

(defn spread-above [min-prct]
  (let [neg-min-prct (- 0.0 min-prct)]
    (fn [{:keys [prct]}]
      (or (> prct min-prct)
          (< prct neg-min-prct)))))


(defn valid-spread [flow]
    ; same as current-v .. need to verify.
  (first-match #(not (nil? (:prct %))) flow))


(defn premium-list [assets]
  (let [flows (map future-premium-f assets)
        tasks (map valid-spread flows)
        tasks (map wrap-blk tasks)
        spreads (m/? (apply m/join vector tasks))]
    spreads))

(premium-list ["BTCUSDT" "ETHUSDT"])
; [{:asset "BTCUSDT", :spot 65701.5, :perp 65674.6, :abs -26.89999999999418, :prct -0.04094274864347721}
;  {:asset "ETHUSDT", :spot 3276.81, :perp 3275.04, :abs -1.7699999999999818, :prct -0.0540159484376568}]

(def spreads
  (premium-list (take 10 asset-symbols-both)))

(def spreads
  (premium-list asset-symbols-both))

   ; (filter (spread-above 0.05) spreads)


