(ns dev.env
  (:require
   [clojure.edn :as edn]
   [quanta.market.asset.db :as asset-db]
   [quanta.market.asset.load :refer [add-lists-to-db]]))

(def secrets
  (-> (str (System/getenv "MYVAULT") "/quanta.edn")
      (slurp)
      (edn/read-string)))

secrets

(def assets
  [; kibot
   {:name "EURUSD" :asset "EUR/USD" :kibot "EURUSD" :category :fx}
   {:name "Microsoft" :asset "MSFT" :kibot "MSFT" :category :equity}])

#_(doall
   (map asset-db/add assets))

(add-lists-to-db)

;(asset-db/instrument-details "EUR/USD")



