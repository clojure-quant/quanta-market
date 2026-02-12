(ns quanta.recipy.eodhd-list-volume
  (:require
   [missionary.core :as m]
   [quanta.market.asset.datahike :refer [get-asset add-update-list]]
   [quanta.market.adapter.eodhd.raw :as raw]))

(defn in-million [n]
  (/ n 1000000.0))

(defn floor [d]
  (Math/floor d))


(defn short [s]
  (when s
    (subs s 0 (min 20 (dec (count s))))))

(defn add-name-exchange-type [{:keys [eodhd-token assetdb] :as _ctx} items]
  (let [one (fn [item]
              (let [asset (get-asset assetdb (:code item))]
                (if asset
                  (assoc item :name (short (:asset/name asset))
                         :exchange (:asset/exchange asset)
                         :type (:asset/type asset))
                  (assoc item :name "-"
                         :exchange "-"
                         :type "-"))))]
    (map one items)))

(defn remove-when [condition predicate list]
  (if condition 
    (remove predicate list)
    list))

(defn filter-when [condition predicate list]
  (if condition
    (filter predicate list)
    list))

(defn add-name-exchange-type-when [ctx condition items]
  (if condition
    (add-name-exchange-type ctx items)
    items))


(defn high-volume-assets [{:keys [eodhd-token] :as ctx}
                          {:keys [turnover-min exchange add-name remove-no-name type]
                           :or {turnover-min 1000000.0
                                exchange "US"
                                add-name false
                                remove-no-name false
                                }
                           }]
  (m/sp
   (->> (m/? (raw/get-day-bulk eodhd-token {:exchange exchange}))
        (map #(assoc % :turnover (* (:close %) (:volume %))))
        (filter #(> (:turnover %) turnover-min))
        (map #(update % :turnover in-million))
        (map #(update % :turnover floor))
        (map #(update % :turnover long))
        (map #(select-keys % [:code :name :exchange :close :turnover]))
        (sort-by :turnover)
        (reverse)
        (add-name-exchange-type-when ctx add-name)
        (filter-when (and add-name type) #(= (:type %) type))
        (remove-when remove-no-name #(= (:name %) "-")))))


(defn add-list-high-volume-assets [{:keys [eodhd-token assetdb] :as ctx}
                                   {:keys [turnover-min exchange add-name remove-no-name list-name]
                                    :as opts
                                    :or {turnover-min 1000000.0
                                         exchange "US"
                                         add-name false
                                         remove-no-name false}}]
  (m/sp
   (let [table (m/? (high-volume-assets ctx opts))
         assets (->> table
                     (map :code)
                     (into []))]
     (add-update-list assetdb {:lists/name list-name :lists/asset assets})
     
     )))



