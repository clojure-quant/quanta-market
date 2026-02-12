(ns quanta.recipy.bybit-asset-db
  (:require
   [tick.core :as t]
   [clojure.string :as str]
   [missionary.core :as m]
   [modular.persist.edn] ; side effects to load edn files
   [modular.persist.protocol :refer [save]]
   [quanta.market.asset.datahike :refer [add-update-asset query-assets]]
   [quanta.market.adapter.bybit.raw :as bb]))

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

(defn normalize-spot [{:keys [symbol priceFilter] :as row}]
  (let [asset symbol]
    {:asset/symbol (str asset ".BB")
     :asset/name asset
     :asset/exchange "BYBIT"
     :asset/category :crypto
     :tick-size (:tickSize priceFilter)
     ; bybit specific
     :asset/bybit symbol
     :asset/bybit-category :spot
     }))

(defn normalize-linear [{:keys [symbol priceFilter] :as row}]
  (let [asset symbol]
    {:asset/symbol (str asset ".P.BB")
     :asset/name asset
     :asset/exchange "BYBIT"
     :asset/category :crypto-future
     ;:tick-size (:tickSize priceFilter)
     ; bybit specific
     :asset/bybit symbol
     :asset/bybit-category :linear
     }))

(defn normalize [category asset]
  (case category 
    "spot" (normalize-spot asset)
    "linear" (normalize-linear asset)
    asset))

(defn download-asset-category [{:keys [assetdb]} {:keys [category]}]
  (m/sp
   (let [assets-raw  (-> (m/? (bb/get-assets category))
                         (:list))
         _ (println "bybit category " category " asset count: " (count assets-raw))
         assets (->> assets-raw
                     (map #(normalize category %))
                     (map #(select-keys % [:asset/symbol :asset/name :asset/exchange :asset/category
                                           :asset/bybit :asset/bybit-category 
                                           ]))
                     (into []))
         filename-raw (str (System/getenv "QUANTASTORE") "/asset/raw/bybit-" category ".edn")
         filename (str (System/getenv "QUANTASTORE") "/asset/bybit-" category ".edn")
         ]
     (println "bybit category " category " asset count: " (count assets-raw))
     (save :edn filename-raw assets-raw)
     (save :edn filename assets)
     (when assetdb
       (add-update-asset assetdb assets))
     assets
     )))


