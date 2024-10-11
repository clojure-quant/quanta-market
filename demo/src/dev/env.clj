(ns dev.env
  (:require
   [clojure.edn :as edn]
   [ta.db.asset.db :as asset-db]))


(def secrets
  (-> "/home/florian/repo/myLinux/myvault/goldly/quanta.edn"
      (slurp)
      (edn/read-string)))


(def assets
  [; kibot
   {:name "EURUSD" :symbol "EUR/USD" :kibot "EURUSD" :category :fx}
   {:name "Microsoft" :symbol "MSFT" :kibot "MSFT" :category :equity}
   ])


(doall
 (map asset-db/add assets))



