(ns demo.asset.db.datahike
  (:require
   [datahike.api :as d]
   [quanta.market.asset.datahike :refer [add-update-asset query-assets]]
   [demo.env-bar :refer [eodhd eodhd-token bardb-nippy dbc]]
   ))


(add-asset-details dbc {:asset/symbol "AAPL"
                        :asset/name "Apple Computer Inc"})

(add-update-asset dbc [{:asset/symbol "AAPL"
                  :asset/name "Apple Computer Inc"}])

(add-update-asset dbc [{:asset/symbol "AAPL"
                  :asset/exchange "NASDAQ"
                  :asset/type :equity}])

(add-update-asset dbc [{:asset/symbol "MO"
                        :asset/name "Altria"
                        :asset/exchange "NYSE"
                        :asset/type :equity}])

(add-update-asset dbc [{:asset/symbol "SPY"
                  :asset/name "Spiders S&P 500 ETF"
                  :asset/exchange "NYSE"
                  :asset/type :etf}])

(add-update-asset dbc [{:asset/symbol "SPY"
                  :asset/name "Spiders S&P 500 ETF"
                  :asset/exchange "NYSE"
                  :asset/type :etf}])


(def query-assets-all
  '[:find [(pull ?id [*]) ...]
    :in $
    :where
    [?id :asset/symbol _]])

(def query-assets-nasdaq
  (conj
   query-assets-all
   '[?id :asset/exchange :NASDAQ]))

(d/q  query-assets-all @dbc)

(def a 34)

`[?id :asset/exchange ~a]

(d/q  query-assets-nasdaq @dbc)


(re-find #"(?i)FOO" "Foobar")
(re-find #"FOO" "barbar")

[(clojure.string/lower-case ?name) ?lname]

(query-assets dbc {:exchange :NASDAQ})
(query-assets dbc {:exchange :NYSE})

(query-assets dbc {:exchange :NYSE :type :stock})
(query-assets dbc {:exchange :NYSE :type :etf})
(query-assets dbc {:type :etf})
(query-assets dbc {:type :stock})
(query-assets dbc {})

(query-assets dbc {:exchange :NYSE :type :etf :q "MO"})

(query-assets dbc {:q "Tr"})

