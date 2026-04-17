(ns quanta.recipy.eodhd-asset-db
  (:require
   [missionary.core :as m]
   [quanta.market.persist :refer [spit-edn slurp-edn]]
   [quanta.market.adapter.eodhd.raw :as raw]
   [quanta.market.adapter.eodhd.ds :refer [convert-asset]]
   [quanta.market.asset.datahike :refer [add-update-asset]]))

(defn asset-stats [assets]
  {:exchanges (->> assets
                   (group-by :Exchange)
                   (map (fn [[k v]] [k (count v)]))
                   (into {}))
   :types (->> assets
               (group-by :Type)
               (map (fn [[k v]] [k (count v)]))
               (into {}))})

(defn get-exchange-assets [{:keys [eodhd-token]} market]
  (raw/get-exchange-assets eodhd-token market))

(defn asset-summary [ctx {:keys [market]}]
  (m/sp
   (let [assets (m/? (get-exchange-assets ctx market))]
     (assoc (asset-stats assets)
            :example (first assets)))))

(defn filter-assets [{:keys [exchanges types]} assets]
  (let [filter-exchange (fn [items]
                          (if exchanges
                            (filter #(contains? exchanges  (:Exchange %)) items)
                            items))
        filter-type (fn [items]
                      (if types
                        (filter #(contains? types  (:Type %)) items)
                        items))]
    (->> assets
         (filter-exchange)
         (filter-type)
         (into []))))

(defn build-asset-edn [{:keys [eodhd-token]} {:keys [market
                                                     _exchanges _types
                                                     filename] :as opts}]
  (m/sp
   (let [assets (m/? (raw/get-exchange-assets eodhd-token market))]
     (->> assets
          (filter-assets opts)
          (into [])
          (spit-edn filename)))))

(defn build-asset-edn-normalized [{:keys [eodhd-token asset-db]}
                                  {:keys [market _exchanges _types
                                          filename] :as opts}]
  (m/sp
   (let [assets  (->> (m/? (raw/get-exchange-assets eodhd-token market))
                      (filter-assets opts)
                      (map convert-asset)
                      (into []))]
     (spit-edn filename assets)
     (when asset-db
       (add-update-asset asset-db assets))
     assets)))

(defn build-exchange-edn [{:keys [eodhd-token]} {:keys [filename] :as _opts}]
  (m/sp
   (let [exchanges (->> (m/? (raw/get-exchanges eodhd-token))
                        (into []))]
     (spit-edn filename exchanges))))



