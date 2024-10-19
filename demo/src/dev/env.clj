(ns dev.env
  (:require
   [clojure.edn :as edn]
   [ta.db.asset.db :as asset-db]))

(def secrets
  (-> (str (System/getenv "MYVAULT") "/quanta.edn")
      (slurp)
      (edn/read-string)))

secrets

(def assets
  [; kibot
   {:name "EURUSD" :symbol "EUR/USD" :kibot "EURUSD" :category :fx}
   {:name "Microsoft" :symbol "MSFT" :kibot "MSFT" :category :equity}])

(doall
 (map asset-db/add assets))

(asset-db/instrument-details "EUR/USD")



