(ns demo.barimport.eodhd
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [modular.persist.edn] ; side effects to load edn files
   [modular.persist.protocol :refer [save loadr]]
   [quanta.bar.protocol :as b]
   [quanta.market.barimport.eodhd.ds :refer [create-import-eodhd]]
   [quanta.market.barimport.eodhd.raw :as raw]
   [demo.env-bar :refer [secrets]]))

(:eodhd secrets)

;; RAW

(def eodhd-token  (:eodhd secrets))

(def d (m/? (raw/get-bars eodhd-token
                          "MCD.US" "2026-01-01" "2026-02-01")))

d
(->> (m/? (raw/get-bars (:eodhd secrets)
                        "MCD.US" "2020-01-01" "2024-03-15"))
     raw/warning)
;; => "Data is limited by one year as you have free subscription"

(m/? (raw/get-day-bulk eodhd-token
                       {:exchange "US"
                        :type "splits"}))
; Bulk requests are prohibited for free users. Please, contact our support team: support@eodhistoricaldata.com

(try (m/? (raw/get-day-bulk eodhd-token {:exchange "US"
                                         :type "splits"}))
     (catch Exception ex
       (println "ex: " ex)
       (println "ex data:" (ex-data ex))
       (println "ex cause:" (ex-cause ex))))

(def exchanges
  (m/? (raw/get-exchanges eodhd-token)))

(count exchanges)

(save :edn "./data/exchanges.edn" exchanges)

(def assets
  (m/? (raw/get-exchange-assets eodhd-token "US")))

(->> assets
     first)
{:Currency "USD",
 :Exchange "US",
 :Code "^TNX",
 :Name "CBOE Interest Rate 10 Year T No",
 :Isin nil,
 :Type "INDEX",
 :Country "USA"}

(->> assets
     (map :Type)
     (into #{}))
;; #{"Unit" "Common Stock" "FUND" "INDEX" "ETF" "BOND" 
;;   "Mutual Fund" "Preferred Stock" "ETC" "Warrant" "Notes"}

(->> assets
     (filter #(= "Common Stock" (:Type %)))
     (group-by :Exchange)
     (map (fn [[k v]] [k (count v)]))
     (into {}))
{"NASDAQ" 3968,
 "NYSE" 2303,
 "AMEX" 16,
 "BATS" 7,
 "PINK" 8943,
 "OTCQX" 556,
 "OTCBB" 3,
 "OTCGREY" 556,
 "NYSE ARCA" 37,
 "OTCQB" 1060,
 "NYSE MKT" 250,
 "OTCMKTS" 51,
 "US" 9,
 "OTCCE" 536}

(->> assets
     (filter #(= "Common Stock" (:Type %)))
     (filter #(contains? #{"NYSE" "NASDAQ"} (:Exchange %)))
     (into [])
     (save :edn "./data/stocks.edn"))

(m/? (raw/get-splits
      "demo"
      {:asset "AAPL.US" :from "1980-01-01" :to "2026-03-20"}))

;[{:date "1987-06-16", :split "2.000000/1.000000"}
; {:date "2000-06-21", :split "2.000000/1.000000"}
; {:date "2005-02-28", :split "2.000000/1.000000"}
; {:date "2014-06-09", :split "7.000000/1.000000"}
; {:date "2020-08-31", :split "4.000000/1.000000"}]

(def eodhd (create-import-eodhd eodhd-token))

(m/? (b/get-bars eodhd
                 {:asset "RPM.AU"
                  :calendar [:us :d]}
                 {:start (t/zoned-date-time "2025-06-01T00:00:00Z")
                  :end (t/zoned-date-time "2026-03-20T00:00:00Z")}))
;; => _unnamed [56 7]:
;;    
;;    |                :date | :open | :high |  :low | :close | :adjusted_close | :volume |
;;    |----------------------|------:|------:|------:|-------:|----------------:|--------:|
;;    | 2024-01-02T00:00:00Z | 0.087 | 0.097 | 0.087 |  0.096 |           0.096 |  135383 |
;;    | 2024-01-03T00:00:00Z | 0.092 | 0.095 | 0.090 |  0.095 |           0.095 |  169381 |
;;    | 2024-01-04T00:00:00Z | 0.090 | 0.090 | 0.089 |  0.089 |           0.089 |   88120 |
;;    | 2024-01-05T00:00:00Z | 0.089 | 0.089 | 0.089 |  0.089 |           0.089 |       0 |
;;    | 2024-01-08T00:00:00Z | 0.090 | 0.090 | 0.090 |  0.090 |           0.090 |   25000 |
;;    | 2024-01-09T00:00:00Z | 0.090 | 0.090 | 0.090 |  0.090 |           0.090 |   71562 |
;;    | 2024-01-10T00:00:00Z | 0.090 | 0.090 | 0.090 |  0.090 |           0.090 |  135000 |
;;    | 2024-01-11T00:00:00Z | 0.090 | 0.090 | 0.090 |  0.090 |           0.090 |       0 |
;;    | 2024-01-12T00:00:00Z | 0.090 | 0.090 | 0.090 |  0.090 |           0.090 |       0 |
;;    | 2024-01-15T00:00:00Z | 0.088 | 0.091 | 0.088 |  0.091 |           0.091 |   62023 |
;;    |                  ... |   ... |   ... |   ... |    ... |             ... |     ... |
;;    | 2024-03-06T00:00:00Z | 0.088 | 0.088 | 0.087 |  0.087 |           0.087 |   10023 |
;;    | 2024-03-07T00:00:00Z | 0.087 | 0.087 | 0.084 |  0.084 |           0.084 |  186854 |
;;    | 2024-03-08T00:00:00Z | 0.087 | 0.087 | 0.084 |  0.086 |           0.086 |   18100 |
;;    | 2024-03-11T00:00:00Z | 0.087 | 0.087 | 0.087 |  0.087 |           0.087 |   11111 |
;;    | 2024-03-12T00:00:00Z | 0.087 | 0.088 | 0.086 |  0.086 |           0.086 |   69083 |
;;    | 2024-03-13T00:00:00Z | 0.087 | 0.087 | 0.086 |  0.086 |           0.086 |   69879 |
;;    | 2024-03-14T00:00:00Z | 0.086 | 0.086 | 0.084 |  0.084 |           0.084 |  118998 |
;;    | 2024-03-15T00:00:00Z | 0.084 | 0.084 | 0.083 |  0.083 |           0.083 |   70977 |
;;    | 2024-03-18T00:00:00Z | 0.084 | 0.084 | 0.083 |  0.083 |           0.083 |   22961 |
;;    | 2024-03-19T00:00:00Z | 0.083 | 0.083 | 0.083 |  0.083 |           0.083 |   71606 |
;;    | 2024-03-20T00:00:00Z | 0.082 | 0.082 | 0.082 |  0.082 |           0.082 |   50000 |


