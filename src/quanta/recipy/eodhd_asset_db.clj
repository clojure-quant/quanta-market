(ns quanta.recipy.eodhd-asset-db
  (:require
   [missionary.core :as m]
   [modular.persist.edn] ; side effects to load edn files
   [modular.persist.protocol :refer [save]]
   [quanta.market.barimport.eodhd.raw :as raw]))

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
                                                     exchanges types
                                                     filename] :as opts}]
  (m/sp
   (let [assets (m/? (raw/get-exchange-assets eodhd-token market))]
     (->> assets
          (filter-assets opts)
          (into [])
          (save :edn filename)))))

(defn build-exchange-edn [{:keys [eodhd-token]} {:keys [filename] :as opts}]
  (m/sp
   (let [exchanges (m/? (raw/get-exchanges eodhd-token))]
     (->> exchanges
          (into [])
          (save :edn filename)))))



