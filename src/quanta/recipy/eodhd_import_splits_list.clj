(ns quanta.recipy.eodhd-import-splits-list
  (:require
   [missionary.core :as m]
   [tablecloth.api :as tc]
   [quanta.missionary :refer [rest-import]]
   [quanta.bar.split.service :refer [save-splits delete-splits]]
   [quanta.market.adapter.eodhd.ds :refer [get-splits]]
   [quanta.market.asset.datahike :refer [get-list]]))

(defn import-splits-list [{:keys [asset-db ss eodhd-token] :as ctx} {:keys [list start end]}]
  (let [assets (-> (get-list asset-db list)
                   :lists/asset)
        opts-seq (->> assets
                      (map (fn [asset]
                             {:asset asset
                              :start start
                              :end end})))
        download-fn (fn [ctx opts]
                      (m/sp (let [splits (m/? (get-splits eodhd-token
                                                          (select-keys opts [:asset])
                                                          (select-keys opts [:start :end])))]
                              ;(println "asset: " (:asset opts) " splits: " (tc/row-count splits))
                              splits)))
        store-fn (fn [_ctx opts data]
                   (m/sp
                    (when (and data (not (= 0 (tc/row-count data))))
                      (let [data-asset (tc/add-column data :asset (:asset opts))]
                        (m/? (delete-splits ss (:asset opts)))
                        (m/? (save-splits ss data-asset))))))]
    (rest-import ctx {:tasks-opts opts-seq
                      :download-fn download-fn
                      :store-fn store-fn
                      :parallel 50
                      :cost 1
                      ;X-RateLimit-Limit: 1000  = per minute
                      :capacity 600 ; initially it is 1000, but to be safe 500.
                      :rate 10 ; 100 per minute = 16 per second, lets go to 10 be safe 
                      })))