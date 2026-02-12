(ns demo.env-bar
  (:require
   [clojure.string :as str]
   [clojure.edn :as edn]
   [quanta.bar.db.nippy :refer [start-bardb-nippy]]
   [quanta.market.asset.db :as asset-db]
   [quanta.market.adapter.eodhd.ds :refer [create-import-eodhd]]
   [quanta.market.asset.datahike :refer [start-asset-db]]))

(defn expand-env-safe [s]
  (str/replace s #"\$\{([^}]+)\}"
               (fn [[match var]]
                 (or (System/getenv var) (throw (ex-info (str "ENV-VAR not found: " var) {}))))))

;(expand-env-safe "${MYVAULT}/quanta.edn")
;(expand-env-safe "${MYVAULT4}/quanta.edn")

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

;(asset-db/instrument-details "EUR/USD")
(def bardb-nippy
  (start-bardb-nippy  (str (System/getenv "QUANTASTORE") "/bardb/eodhd-nippy/")))

(def eodhd-token  (:eodhd secrets))

(def eodhd (create-import-eodhd (:eodhd secrets)))

(def assetdb (start-asset-db (str (System/getenv "QUANTASTORE") "/assetdb")))

(def ctx {:eodhd-token eodhd-token
          :eodhd eodhd
          :bardb bardb-nippy
          :assetdb assetdb})