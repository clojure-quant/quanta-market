(ns demo.asset.db.datahike
  (:require
   [datahike.api :as d]
   [quanta.market.asset.datahike :refer [start-asset-db add-asset-details]]))

(def dbc (start-asset-db "./data/datahike"))

(add-asset-details dbc {:asset/symbol "AAPL"
                        :asset/name "Apple Computer Inc"})

(d/transact dbc [{:asset/symbol "AAPL"
                  :asset/name "Apple Computer Inc"}])

(d/transact dbc [{:asset/symbol "AAPL"
                  :asset/exchange :NASDAQ
                  :asset/type :stock}])

(d/transact dbc [{:asset/symbol "MO"
                  :asset/name "Altria"
                  :asset/exchange :NYSE
                  :asset/type :stock}])

(d/transact dbc [{:asset/symbol "SPY"
                  :asset/name "Spiders S&P 500 ETF"
                  :asset/exchange :NYSE
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

(defn conj-key-when [query k v]
  (if v
    (conj query `[~'?id ~k ~v])
    query))

(defn conj-q-when [query q]
  (if q
    (let [r (re-pattern (str "(?i)" q))]
      (-> query
          (conj  `[~'?id :asset/name ~'?name])
          (conj  `[(re-find ~r ~'?name)])))
    query))

(java.util.regex.Pattern/ "asdfg")

(type #"asdf")

(conj-q-when [] "a")

[?e :asset/name ?name]
[(clojure.string/lower-case ?name) ?lname]

(defn query-assets [dbconn {:keys [q exchange type]}]
  (let [query 4]
    (-> '[:find [(pull ?id [*]) ...]
          :in $
          :where
          [?id :asset/symbol _]]
        (conj-key-when :asset/type type)
        (conj-key-when :asset/exchange exchange)
        (conj-q-when q)
        (d/q @dbconn))
    ;(println "query: " query)
    ))

(re-find #"(?i)foo" "Foobar")
(re-find #"foo" "barbar")

[(clojure.string/lower-case ?name) ?lname]

(query-assets dbc {:exchange :NASDAQ})
(query-assets dbc {:exchange :NYSE})

(query-assets dbc {:exchange :NYSE :type :stock})
(query-assets dbc {:exchange :NYSE :type :etf})
(query-assets dbc {:type :etf})
(query-assets dbc {:type :stock})

(query-assets dbc {:exchange :NYSE :type :etf :q "S"})

(query-assets dbc {:q "S"})

