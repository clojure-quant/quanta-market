(ns quanta.notebook.asset-db.datahike-query
  (:require
   [datahike.api :as d]
   [quanta.market.asset.datahike :refer [add-update-asset query-assets get-asset
                                         add-update-list get-list
                                         provider->asset asset->provider
                                         exchanges categories]]
   [modular.system :refer [system]]))

(def asset-db (get-in system [:ctx :asset-db]))

; query assets

(def query-assets-all
  '[:find [(pull ?id [*]) ...]
    :in $
    :where
    [?id :asset/symbol _]])

(d/q  query-assets-all @asset-db)

(def query-exchanges
  '[:find [(pull ?id [:asset/exchange]) ...]
    :in $
    :where
    [?id :asset/exchange _]])

(->> (d/q  query-exchanges @asset-db)
     (map :asset/exchange)
     (into #{}))

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

(d/q  query-or @asset-db #"(?i)altr")
(d/q  query-or @asset-db #"(?i)msft")

(re-find #"(?i)FOO" "Foobar")
(re-find #"FOO" "barbar")

(defn qc [q]
  (let [r (query-assets asset-db q)]
    [q (count r)]))

(qc {:exchange "NASDAQ"})

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

; 14676

(d/transact asset-db [[:db/retractEntity 14676]])
