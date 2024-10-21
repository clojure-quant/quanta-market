(ns dev.asset.normalized.bybit
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [missionary.core :as m]))

(defn load-raw [category]
  (-> (str "../resources/asset/raw/bybit-" (name category) ".edn")
      slurp
      edn/read-string))

(defn save-category [c assets]
  (spit (str "../resources/asset/bybit-" (name c) ".edn") (pr-str assets)))

;; SPOT

(->> (load-raw :spot)
     first)
;; => {:lotSizeFilter
;;     {:basePrecision "0.000001",
;;      :maxOrderAmt "4000000",
;;      :maxOrderQty "71.73956243",
;;      :minOrderAmt "1",
;;      :quotePrecision "0.00000001",
;;      :minOrderQty "0.000048"},
;;     :innovation "0",
;;     :symbol "BTCUSDT",
;;     :quoteCoin "USDT",
;;     :priceFilter {:tickSize "0.01"},
;;     :baseCoin "BTC",
;;     :marginTrading "utaOnly",
;;     :status "Trading",
;;     :riskParameters {:marketParameter "0.03", :limitParameter "0.03"}}

(->> (load-raw :spot)
     (map :symbol)
     (group-by count)
     keys)
;; => (7 6 8 9 5 10 12 11)

;; bybit spot symbols have minimum 5 letters, so no conflict with kibot

(defn normalize-spot [{:keys [symbol priceFilter] :as row}]
  (let [asset symbol]
    {:asset asset
     :category :crypto-spot
     :bybit symbol
     :bybit-category :spot
     :tick-size (:tickSize priceFilter)}))

(->> (load-raw :spot)
     (map normalize-spot)
     (save-category :spot))

;; LINEAR

(->> (load-raw :linear)
     (map :symbol)
     (group-by count)
     keys)
;; => (7 13 6 17 12 19 11 9 5 14 16 10 18 8)
;; bybit spot symbols have minimum 5 letters, so no conflict with kibot

(defn normalize-linear [{:keys [symbol priceFilter] :as row}]
  (let [asset symbol]
    {:asset (str asset ".P")
     :category :crypto-future
     :bybit symbol
     :bybit-category :linear
     ;:tick-size (:tickSize priceFilter)
     }))

(->> (load-raw :linear)
     (filter #(= "LinearPerpetual" (:contractType %)))
     (map normalize-linear)
     (save-category :linear))





