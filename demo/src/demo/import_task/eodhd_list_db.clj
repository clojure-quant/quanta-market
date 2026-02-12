(ns demo.asset.eodhd-list-db
  (:require
   [clojure.pprint :refer [print-table]]
   [tick.core :as t]
   [missionary.core :as m]
   [tablecloth.api :as tc] 
   [quanta.recipy.eodhd-list-volume :refer [add-list-high-volume-assets]]
   [demo.env-bar :refer [ctx]]))

(def etfs (m/? (add-list-high-volume-assets ctx {:exchange "US"
                                                 :turnover-min 10000000.0
                                                 :add-name true
                                                 :remove-no-name true
                                                 :type :etf
                                                 :list-name "etf-10mio"})))

(def equities (m/? (add-list-high-volume-assets ctx {:exchange "US"
                                                     :turnover-min 10000000.0
                                                     :add-name true
                                                     :remove-no-name true
                                                     :type :equity
                                                     :list-name "equity-10mio"})))

(->> equities
    :tx-data
    reverse
    (take 3)
;    count
 )
 ;[2419 "ALG"]



