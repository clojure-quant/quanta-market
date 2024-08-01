(ns quanta.market.broker.bybit.trade
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [mix]]
   [quanta.market.broker.bybit.trade-update :refer [create-trade-update-feed trade-update-msg-flow]]
   [quanta.market.broker.bybit.trade-action :refer [create-trade-action]]))


(defrecord bybit-trade [opts order orderupdate]
  p/tradeaccount


)


(defmethod p/create-tradeaccount :bybit
  [{:keys [creds mode] :as opts}]
  (assert creds "bybit trade needs :creds")
  (assert mode "bybit trade needs :mode (:test :main)")
  (info "creating bybit trade: " opts)
  (let [order (create-trade-action opts) 
        orderupdate (create-trade-update-feed opts)]
    (bybit-trade. opts order orderupdate)))

