(ns demo.env-bar
  (:require
   [clojure.string :as str]
   [clojure.edn :as edn]
   [modular.env :refer [env]]
   [modular.log :refer [timbre-config!]]
   [quanta.bar.db.nippy :refer [start-bardb-nippy]]
   [quanta.bar.db.duck :as duck]
   [quanta.bar.split.service :refer [start-split-service]]
   [quanta.market.adapter.eodhd.ds :refer [create-import-eodhd]]
   [quanta.market.asset.datahike :refer [start-asset-db]]))

(timbre-config!
 {:min-level [[#{"org.eclipse.jetty.*"} :warn]
              [#{"modular.oauth2.token.refresh"} :warn]
              [#{"*"} :info]]
  :appenders {:default {:type :console-color}}})

(def secrets
  (-> (env "${MYVAULT}/quanta.edn")
      (slurp)
      (edn/read-string)))

secrets

(def assetdb (start-asset-db (env "${QUANTASTORE}/assetdb")))

(def bardb-nippy
  (start-bardb-nippy (env "${QUANTASTORE}/bardb/eodhd-nippy/")))

(def bardb-duck (duck/start-bardb-duck (env "${QUANTASTORE}/bardb/eodhd.ddb")))

(def ss (start-split-service {:bardb bardb-duck}))

(def eodhd-token  (:eodhd secrets))

(def eodhd (create-import-eodhd (:eodhd secrets)))

(def ctx {:assetdb assetdb
          :bardb bardb-duck ;bardb-nippy
          :ss ss
          :eodhd-token eodhd-token
          :eodhd eodhd})



