(ns quanta.recipy.eodhd-import-bars-list
  (:require
   [missionary.core :as m]
   ;[tablecloth.api :as tc]
   [quanta.missionary :refer [rest-import]]
   [quanta.bar.protocol :as b]
   [quanta.market.asset.datahike :refer [get-list]]))

(defn import-bars-list [ctx {:keys [list calendar start end]}]
  (let [assets (-> (get-list (:assetdb ctx) list)
                   :lists/asset)
        opts-seq (->> assets
                      (map (fn [asset]
                             {:asset asset
                              :calendar calendar
                              :start start
                              :end end})))
        download-fn (fn [ctx opts]
                      (m/sp (let [bars (m/? (b/get-bars (:eodhd ctx)
                                                        (select-keys opts [:asset :calendar])
                                                        (select-keys opts [:start :end])))]
                              ;(println "asset: " (:asset opts) " bars: " (tc/row-count bars))
                              bars)))
        store-fn (fn [ctx opts data]
                   (b/append-bars (:bardb ctx)
                                  (select-keys opts [:asset :calendar])
                                  data))]
    (rest-import ctx {:tasks-opts opts-seq
                      :download-fn download-fn
                      :store-fn store-fn
                      :parallel 50
                      :cost 1
                      ;X-RateLimit-Limit: 1000  = per minute
                      :capacity 600 ; initially it is 1000, but to be safe 500.
                      :rate 10 ; 100 per minute = 16 per second, lets go to 10 be safe 
                      })))