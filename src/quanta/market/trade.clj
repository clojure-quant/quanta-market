(ns quanta.market.trade
  (:require
   [missionary.core :as m]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.connection :as c]
   [quanta.market.util :refer [mix]]
))


(defrecord trade-manager [cm]
   p/trade
  (order-create! [{:keys [cm] :as this} 
                  {:keys [account] :as order}]
                 (if-let [a (c/get-account cm account)]
                   (p/order-create! a order)))
  (order-cancel! [{:keys [cm] :as this} 
                  {:keys [account] :as order-cancel}]
                 (if-let [a (c/get-account cm account)]
                   (p/order-cancel! a order-cancel)))
  (order-update-msg-flow [{:keys [cm] :as this} ]
                         (let [account-flows (map p/order-update-msg-flow (vals (:accounts cm)))]
                           (apply mix account-flows)))
  (order-update-flow [{:keys [cm] :as this} ]
                     (let [account-flows (map p/order-update-flow (vals (:accounts cm)))]
                       (apply mix account-flows))))


  
(defn trade-manager-start [cm]
  (trade-manager. cm))

(defn trade-manager-stop [{:keys [db accounts] :as this}]
  ;
  )
