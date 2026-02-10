(ns demo.asset.eodhd-asset-db
  (:require
   [missionary.core :as m]
   [quanta.recipy.eodhd-asset-db :refer [get-exchange-assets asset-summary asset-stats filter-assets 
                                         build-asset-edn build-asset-edn-normalized build-exchange-edn]]
   [demo.env-bar :refer [ctx bardb-nippy]]))

(m/? (asset-summary ctx {:market "US"}))

{:example
 {:Currency "USD" :Exchange "US" :Code "^TNX" :Name "CBOE Interest Rate 10 Year T No"
  :Isin nil :Type "INDEX"  :Country "USA"}
 :types
 {"Unit" 27 "Common Stock" 18296 "FUND" 21451 "INDEX" 1 "ETF" 5243  "BOND" 2
  "Mutual Fund" 3216 "Preferred Stock" 729  "ETC" 2 "Warrant" 192 "Notes" 44}
 :exchanges
 {"BATS" 1082 "PINK" 9089 "NMFQS" 24330 "NYSE" 3365 "AMEX" 37 "OTCQX" 562 "OTCBB" 7
  "OTCGREY" 628 "NASDAQ" 5507  "NYSE ARCA" 2592 "OTCQB" 1064 "NYSE MKT" 300
  "OTCMKTS" 54  "US" 51 "OTCCE" 535}}


#{"NYSE" "NASDAQ"}

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
                           :filename "./data/raw/eodhd-etf.edn"}))

(m/? (build-asset-edn ctx {:market "US" 
                           :types #{"Common Stock"}
                           :exchanges #{"NYSE" "NASDAQ" "AMEX" "NYSE MKT"}
                           :filename "./data/raw/eodhd-stocks.edn"
                           }))



(m/? (build-exchange-edn ctx {:filename "./data/raw/eodhd-exchanges.edn"}))


;; NORMALIZED ASSETS AND DB ACCESS

(m/? (build-asset-edn-normalized ctx {:market "US"
                           :types #{"ETF"}
                           :filename "./data/eodhd-etf.edn"}))

(m/? (build-asset-edn-normalized ctx {:market "US"
                                      :types #{"Common Stock"}
                                      :exchanges #{"NYSE" "NASDAQ" "AMEX" "NYSE MKT"}
                                      :filename  "./data/eodhd-stocks.edn"}))


