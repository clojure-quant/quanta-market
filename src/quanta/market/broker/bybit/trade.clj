(ns quanta.market.broker.bybit.trade
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [mix]]
   [quanta.market.broker.bybit.trade-update :refer [create-trade-update-feed]]
   [quanta.market.broker.bybit.trade-action :refer [create-trade-action]]))

(defrecord bybit-trade [opts order-s orderupdate-s]
  ; action
  p/trade-action
  (trade-action-flow [this]
    (p/trade-action-flow order-s))
  (trade-action-msg-flow [this]
    (p/trade-action-msg-flow order-s))
  (order-create! [this order]
    (p/order-create! order-s order))
  (order-cancel! [this order-cancel]
    (p/order-cancel! order-s order-cancel))
  ; update
  p/trade-update
  (orderupdate-msg-flow [this]
    (p/orderupdate-msg-flow orderupdate-s))
  (orderupdate-flow [this]
    (p/orderupdate-flow orderupdate-s))
  ; account
  p/trade-account
  (account-flow [this]
    (mix (p/trade-action-flow this)
         (p/orderupdate-flow this)))
  (account-msg-flow [this]
    (mix (p/trade-action-msg-flow this)
         (p/orderupdate-msg-flow this))))

(defmethod p/create-tradeaccount :bybit
  [{:keys [creds mode] :as opts}]
  (assert creds "bybit trade needs :creds")
  (assert mode "bybit trade needs :mode (:test :main)")
  (info "creating bybit trade: " opts)
  (let [order-s (create-trade-action opts)
        orderupdate-s (create-trade-update-feed opts)]
    (bybit-trade. opts order-s orderupdate-s)))

