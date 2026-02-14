(ns demo.asset.datahike
  (:require
   [datahike.api :as d]
   [quanta.market.asset.datahike :refer [add-update-asset query-assets get-asset
                                         add-update-list get-list
                                         provider->asset asset->provider
                                         exchanges categories]]
   [demo.env-bar :refer [eodhd eodhd-token bardb-nippy assetdb]]))

{:symbol "ENSUSDT.BB",
 :name "ENSUSDT",
 :exchange "BYBIT",
 :category :crypto,
 :bybit "ENSUSDT",
 :bybit-category :spot}

;; add/modify asset

(add-update-asset assetdb {:asset/symbol "AAPL"
                           :asset/name "Apple Computer Inc"})

(add-update-asset assetdb [{:asset/symbol "AAPL"
                            :asset/name "Apple Computer Inc"}])

(add-update-asset assetdb [{:asset/symbol "AAPL"
                            :asset/exchange "NASDAQ"
                            :asset/category :equity}])

(add-update-asset assetdb [{:asset/symbol "MO"
                            :asset/name "Altria"
                            :asset/exchange "NYSE"
                            :asset/category :equity}])

(add-update-asset assetdb [{:asset/symbol "SPY"
                            :asset/name "Spiders S&P 500 ETF"
                            :asset/exchange "NYSE"
                            :asset/category :etf}])

(add-update-asset assetdb [{:asset/symbol "SPY"
                            :asset/name "Spiders S&P 500 ETF"
                            :asset/exchange "NYSE"
                            :asset/category :etf}])

;; details on single asset

(get-asset assetdb "MSFT")
(get-asset assetdb "000")

(asset->provider assetdb :bybit "ENSUSDT.BB")
;; [:spot "ENSUSDT"]
(provider->asset assetdb :bybit [:spot "ENSUSDT"])
;; "ENSUSDT.BB"

; query assets

(def query-assets-all
  '[:find [(pull ?id [*]) ...]
    :in $
    :where
    [?id :asset/symbol _]])

(d/q  query-assets-all @assetdb)

(def query-exchanges
  '[:find [(pull ?id [:asset/exchange]) ...]
    :in $
    :where
    [?id :asset/exchange _]])

(->> (d/q  query-exchanges @assetdb)
     (map :asset/exchange)
     (into #{}))

(exchanges assetdb)

(categories assetdb)

(def query-or
  '[:find [(pull ?id [*]) ...]
    :in $ reg?
    :where
    [?id :asset/symbol ?as]
    [?id :asset/category :equity]
    [?id :asset/name ?an]
    #_(or-join [?an ?as]
               [(re-find reg? ?an)]
               [(re-find reg? ?as)])])

(d/q  query-or @assetdb #"(?i)altr")
(d/q  query-or @assetdb #"(?i)msft")

(re-find #"(?i)FOO" "Foobar")
(re-find #"FOO" "barbar")

(defn qc [q]
  (let [r (query-assets assetdb q)]
    [q (count r)]))

(qc {:exchange "NASDAQ"})

(query-assets assetdb {:q "MSFT"})
(qc {:q "MSFT"})

(map qc [{}
         {:category :etf}
         {:category :equity}
         {:category :crypto}
         {:category :fx}
         {:exchange "NASDAQ"}
         {:exchange "NYSE"}
         {:exchange "BYBIT"}
         {:exchange "NYSE" :category :equity}
         {:exchange "NYSE" :category :etf}
         {:exchange "BYBIT" :category :crypto-future}
         {:q "Tr"}
         {:exchange "NYSE" :category :etf :q "MO"}])

;; lists 

(add-update-list assetdb {:lists/name "flo" :lists/asset ["MSFT" "SPY"]})

(get-list assetdb "flo")

(-> (get-list assetdb "etf-10mio")
    :lists/asset
    count)

(get-list assetdb "etf-10mio")
;; asset list now contains duplicates.

(-> (get-list assetdb "equity-20mio")
    :lists/asset
    count)



