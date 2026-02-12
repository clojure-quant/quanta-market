(ns quanta.recipy.eodhd-import-splits-list
  (:require
   [missionary.core :as m]
   [tablecloth.api :as tc]
   [quanta.missionary :refer [rest-import]]
   [quanta.market.adapter.eodhd.ds :refer [get-splits]]
   [quanta.market.asset.datahike :refer [get-list]]))

(defonce splits-a (atom (tc/dataset)))

(defn import-splits-list [ctx {:keys [list start end]}]
  (let [assets (-> (get-list (:assetdb ctx) list)
                   :lists/asset)
        opts-seq (->> assets
                      (map (fn [asset]
                             {:asset asset 
                              :start start
                              :end end})))
        download-fn (fn [ctx opts]
                      (m/sp (let [splits (m/? (get-splits (:eodhd-token ctx)
                                                          (select-keys opts [:asset ])
                                                          (select-keys opts [:start :end])))]
                              ;(println "asset: " (:asset opts) " splits: " (tc/row-count splits))
                              splits
                              )))
        store-fn (fn [ctx opts data]
                   (m/sp 
                    (when (and data (not (= 0 (tc/row-count data))))
                      (let [data-asset (tc/add-column data :asset (:asset opts))]
                        (swap! splits-a tc/concat data-asset)))))]
    (rest-import ctx {:tasks-opts opts-seq
                      :download-fn download-fn
                      :store-fn store-fn
                      :parallel 50
                      :cost 1
                      ;X-RateLimit-Limit: 1000  = per minute
                      :capacity 600 ; initially it is 1000, but to be safe 500.
                      :rate 10 ; 100 per minute = 16 per second, lets go to 10 be safe 
                      })))