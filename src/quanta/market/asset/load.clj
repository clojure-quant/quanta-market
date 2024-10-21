(ns quanta.market.asset.load
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [quanta.market.asset.db :as db]))

(defn load-assets [provider-category]
  (-> (str "asset/" provider-category ".edn")
      (io/resource)
      slurp
      edn/read-string))

(def lists ["kibot-forex"
            "kibot-future"
            "kibot-etf"
            "bybit-spot"
            "bybit-linear"])

(defn load-lists []
  (->>  (map load-assets lists)
        (apply concat)))

(defn add-lists-to-db []
  (let [assets (load-lists)]
    (doall (map db/add assets))
    (count assets)))

