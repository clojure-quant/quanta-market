(ns demo.asset.eodhd-asset-db
  (:require
   [missionary.core :as m]
   [quanta.recipy.eodhd-asset-db :refer [get-exchange-assets asset-summary asset-stats filter-assets
                                         build-asset-edn build-asset-edn-normalized build-exchange-edn]]
   [demo.env-bar :refer [ctx bardb-nippy]]))

(m/? (asset-summary ctx {:market "US"}))

(m/? (asset-summary ctx {:market "VI"}))

{:example
 {:Currency "USD" :Exchange "US" :Code "^TNX" :Name "CBOE Interest Rate 10 Year T No"
  :Isin nil :Type "INDEX" :Country  "USA"}
 :types
 {"Unit" 27 "Common Stock" 18296 "FUND" 21451 "INDEX" 1 "ETF" 5243  "BOND" 2
  "Mutual Fund" 3216 "Preferred Stock" 729  "ETC" 2 "Warrant" 192 "Notes" 44}
 :exchanges
 {"NYSE" 3365 "NYSE ARCA" 2592 "NYSE MKT" 300 "AMEX" 37
  "NASDAQ" 5507 "BATS" 1082
  "PINK" 9089 "NMFQS" 24330   "OTCQX" 562 "OTCBB" 7
  "OTCGREY" 628    "OTCQB" 1064
  "OTCMKTS" 54  "US" 51 "OTCCE" 535}}

(->> (m/? (get-exchange-assets ctx "US"))
     (filter #(= "BATS" (:Exchange %)))
     ;(filter #(= "SBAR" (:Code %)))
     )
(->> (m/? (get-exchange-assets ctx "US"))
     (filter-assets {:types #{"ETF"}})
     (asset-stats))

(->> (m/? (get-exchange-assets ctx "US"))
     (filter-assets {:types #{"FUND"}})
     (asset-stats))

(->> (m/? (get-exchange-assets ctx "US"))
     (filter-assets {:types #{"Common Stock"}})
     (asset-stats))

(->> (m/? (get-exchange-assets ctx "US"))
     (filter-assets {:types #{"Common Stock"}
                     :exchanges #{"NYSE MKT"}})
     ;(asset-stats)
     )

(->> (m/? (get-exchange-assets ctx "VI"))
     ;(filter-assets {:types #{"Common Stock"}})
     (asset-stats))

;; RAW ASSET LISTS

(m/? (build-asset-edn ctx {:market "US"
                           :types #{"ETF"}
                           :filename (str (System/getenv "QUANTASTORE") "/asset/raw/eodhd-etf.edn")}))

(m/? (build-asset-edn ctx {:market "US"
                           :types #{"Common Stock"}
                           :exchanges #{"NYSE" "NASDAQ" "AMEX" "NYSE MKT"}
                           :filename (str (System/getenv "QUANTASTORE") "/asset/raw/eodhd-stocks.edn")}))

(m/? (build-exchange-edn ctx {:filename (str (System/getenv "QUANTASTORE") "/asset/raw/eodhd-exchanges.edn")}))

;; NORMALIZED ASSETS AND DB ACCESS

(m/? (build-asset-edn-normalized ctx {:market "US"
                                      :types #{"ETF"}
                                      :filename (str (System/getenv "QUANTASTORE") 
                                                     "/asset/eodhd-etf.edn")}))

(m/? (build-asset-edn-normalized ctx {:market "US"
                                      :types #{"Common Stock"
                                               "Preferred Stock"}
                                      :exchanges #{"NYSE" "NYSE MKT" "NYSE ARCA"
                                                   "NASDAQ" "AMEX"}
                                      :filename  (str (System/getenv "QUANTASTORE")
                                                      "/asset/eodhd-stocks.edn")}))

