(ns quanta.market.trade
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [mix]]
   ; bring default implementations into scope:
   [quanta.market.broker.bybit.tradeaccount]
   ))

(defn get-tradeaccount [{:keys [tradeaccounts] :as _this} account-id]
  (get tradeaccounts account-id))

(defrecord trade-manager [tradeaccounts]
   p/tradeaccount
  (start-trade [this]
     (doall (map p/start-trade (vals tradeaccounts)))
     (keys tradeaccounts))
  (stop-trade [this]
     (doall (map p/stop-trade (vals tradeaccounts)))
      (keys tradeaccounts))
  (order-create! [this {:keys [account] :as order}]
                 (if-let [ta (get-tradeaccount this account)]
                   (p/order-create! ta order)))
  (order-cancel! [this {:keys [account] :as order-cancel}]
                 (if-let [ta (get-tradeaccount this account)]
                   (p/order-cancel! ta order-cancel)))
  (order-update-msg-flow [this]
                         (let [account-flows (map p/order-update-msg-flow (vals tradeaccounts))]
                           (apply mix account-flows)))
  (order-update-flow [this]
                     (let [account-flows (map p/order-update-flow (vals tradeaccounts))]
                       (apply mix account-flows))))


(defn create-account [[id opts]]
  [id (p/create-tradeaccount (assoc opts :account-id id))])

(defn create-accounts [accounts]
  (->> accounts
       (map create-account)
       (into {})))

(defn trade-manager-start [accounts]
  (let [tradefeeds (create-accounts accounts)]
    (trade-manager. tradefeeds)))

  
(defn trade-manager-stop [this]
  (p/stop-trade this)
  ;
  )
