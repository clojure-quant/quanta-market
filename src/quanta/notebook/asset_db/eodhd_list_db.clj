(ns quanta.notebook.asset-db.eodhd-list-db
  (:require
   [clojure.pprint :refer [print-table]]
   [tick.core :as t]
   [missionary.core :as m]
   [tablecloth.api :as tc]
   [quanta.recipy.eodhd-list-volume :refer [add-list-high-volume-assets]]
   [modular.system :refer [system]]))

(def ctx (:ctx system))

(m/? (add-list-high-volume-assets ctx {:exchange "US"
                                       :turnover-min 10000000.0
                                       :add-name true
                                       :remove-no-name true
                                       :type :etf
                                       :list-name "etf-10mio"}))

(m/? (add-list-high-volume-assets ctx {:exchange "US"
                                       :turnover-min 100000000.0
                                       :add-name true
                                       :remove-no-name true
                                       :type :equity
                                       :list-name "equity-100mio"}))

(m/? (add-list-high-volume-assets ctx {:exchange "US"
                                       :turnover-min 20000000.0
                                       :add-name true
                                       :remove-no-name true
                                       :type :equity
                                       :list-name "equity-20mio"}))

(m/? (add-list-high-volume-assets ctx {:exchange "US"
                                       :turnover-min 10000000.0
                                       :add-name true
                                       :remove-no-name true
                                       :type :equity
                                       :list-name "equity-10mio"}))
