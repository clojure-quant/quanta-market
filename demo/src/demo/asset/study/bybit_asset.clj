(ns dev.asset.study.bybit-asset
  (:require
   [tick.core :as t]
   [clojure.string :as str]
   [missionary.core :as m]
   [quanta.market.adapter.bybit.raw :as bb]))

(->> (m/? (bb/get-assets "spot"))
     :list
     (map :symbol)
     (filter #(str/starts-with? %  "BTC")))
  ;; => ("BTCUSDT" "BTCUSDC" "BTC3SUSDT" "BTC3LUSDT" "BTCDAI" "BTCBRZ" "BTCEUR" "BTCUSDE" "BTCBRL" "BTCPLN" "BTCTRY")

(->> (m/? (bb/get-assets "linear"))
     :list
     (map :symbol)
     (filter #(str/starts-with? %  "BTC")))
  ;; => ("BTC-01NOV24"
  ;;     "BTC-18OCT24"
  ;;     "BTC-25OCT24"
  ;;     "BTC-26SEP25"
  ;;     "BTC-27DEC24"
  ;;     "BTC-27JUN25"
  ;;     "BTC-28MAR25"
  ;;     "BTC-29NOV24"
  ;;     "BTCPERP"
  ;;     "BTCUSDT")

(->> (m/? (bb/get-assets "linear"))
     :list
     (map :symbol)
     (filter #(str/starts-with? %  "BTC")))

(->> (m/? (bb/get-assets "linear"))
     :list
     (filter #(= "BTCUSDT" (:symbol %))))
;; => ({:priceScale "2",
;;      :deliveryFeeRate "",
;;      :deliveryTime "0",
;;      :lotSizeFilter
;;      {:postOnlyMaxOrderQty "1190.000",
;;       :maxOrderQty "1190.000",
;;       :maxMktOrderQty "119.000",
;;       :qtyStep "0.001",
;;       :minNotionalValue "5",
;;       :minOrderQty "0.001"},
;;      :launchTime "1584230400000",
;;      :symbol "BTCUSDT",
;;      :quoteCoin "USDT",
;;      :unifiedMarginTrade true,
;;      :fundingInterval 480,
;;      :isPreListing false,
;;      :priceFilter {:maxPrice "199999.80", :tickSize "0.10", :minPrice "0.10"},
;;      :lowerFundingRate "-0.00375",
;;      :baseCoin "BTC",
;;      :status "Trading",
;;      :preListingInfo nil,
;;      :upperFundingRate "0.00375",
;;      :leverageFilter {:maxLeverage "100.00", :minLeverage "1", :leverageStep "0.01"},
;;      :settleCoin "USDT",
;;      :copyTrading "both",
;;      :contractType "LinearPerpetual"})

(->> (m/? (bb/get-assets "linear"))
     :list
     (filter #(= "BTCUSDT" (:symbol %)))
     first
     :launchTime
     parse-long
     t/instant)
;; => #time/instant "2020-03-15T00:00:00Z"

(->> (m/? (bb/get-assets "spot"))
     :list
     ;(filter #(= "BTCUSDT" (:symbol %)))
     first
     ;:launchTime
     ;parse-long
     ;t/instant
     )
(->> (m/? (bb/get-assets "inverse"))
     :list
     (map :symbol)
     (filter #(str/starts-with? %  "BTC")))
;; => ("BTCUSD" "BTCUSDH25" "BTCUSDZ24")
