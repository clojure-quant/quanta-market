(ns quanta.recipy.eodhd-list-volume
  (:require
   [missionary.core :as m]
   [quanta.market.asset.datahike :refer [get-asset]]
   [quanta.market.adapter.eodhd.raw :as raw]))

(defn in-million [n]
  (/ n 1000000.0))

(defn floor [d]
  (Math/floor d))

(defn high-volume-assets [{:keys [eodhd-token] :as _ctx}
                          {:keys [turnover-min exchange]
                           :or {turnover-min 1000000.0
                                exchange "US"}}]
  (m/sp
   (let [market-summary  (m/? (raw/get-day-bulk eodhd-token
                                                {:exchange exchange}))]
     (->> market-summary
          (map #(assoc % :turnover (* (:close %) (:volume %))))
          (filter #(> (:turnover %) turnover-min))
          (map #(update % :turnover in-million))
          (map #(update % :turnover floor))
          (map #(update % :turnover long))
          (map #(select-keys % [:code :name :exchange :close :turnover]))
          (sort-by :turnover)
          (reverse)))))

   ;(map add-exchange-name)
   ;(map #(update % :name short))

(defn short [s]
  (when s
    (subs s 0 (min 20 (dec (count s))))))

(defn add-name-exchange-type [{:keys [eodhd-token dbc] :as _ctx} items]
  (let [one (fn [item]
              (let [asset (get-asset dbc (:code item))]
                (if asset
                  (assoc item :name (short (:asset/name asset))
                         :exchange (:asset/exchange asset)
                         :type (:asset/type asset))
                  (assoc item :name "-"
                         :exchange "-"
                         :type "-"))))]
    (map one items)))

