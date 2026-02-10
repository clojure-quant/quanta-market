(ns demo.env-bar
  (:require
   [clojure.edn :as edn]
   [quanta.bar.db.nippy :refer [start-bardb-nippy]]
   [quanta.market.asset.db :as asset-db]
   [quanta.market.asset.load :refer [add-lists-to-db]]
   [quanta.market.barimport.eodhd.ds :refer [create-import-eodhd]]
   ))

(def secrets
  (-> ;(str (System/getenv "MYVAULT") "/quanta.edn")
   "/home/florian/repo/myLinux/myvault/quanta.edn"
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
(def bardb-nippy
  (start-bardb-nippy "./data/nippy/"))


(def eodhd-token  (:eodhd secrets))

(def eodhd (create-import-eodhd (:eodhd secrets)))

(def ctx {:eodhd-token eodhd-token 
          :eodhd eodhd
          :bardb bardb-nippy
          })