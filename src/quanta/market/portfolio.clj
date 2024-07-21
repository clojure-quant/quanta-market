(ns quanta.market.portfolio
  (:require
   [missionary.core :as m]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [nano-id.core :refer [nano-id]]
   [tick.core :as t]
   [quanta.market.util :refer [flow-sender start-logging mix]]
   [quanta.market.protocol :as p]
   [quanta.market.trade.schema :as s]
   [quanta.market.trade.order-state :as om :refer [order-manager-start
                                            order-manager-stop
                                            ]]))


(defn wrap-order-update-flow [f]
  (m/eduction (map (fn [r] {:order-update r})) f))

(defn wrap-order-flow [f]
  (m/eduction (map (fn [r] {:order r})) f))


(defn create-flows [tm]
  (let [order-create-flow-sender (flow-sender)
        order-flow (:flow order-create-flow-sender)
        send-new-order-to-flow (fn [order]
                                 (warn "creating new order in db: " order)
                                 ((:send order-create-flow-sender) order))
        wrapped-orderupdate-flow (wrap-order-update-flow
                                  (p/order-update-flow tm))
        order-orderupdate-flow (mix wrapped-orderupdate-flow order-flow)]
  {:order-orderupdate-flow order-orderupdate-flow
   :send-new-order-to-flow send-new-order-to-flow}))


(defn portfolio-manager-start
  "starts the porfolio manager.
   db is optional. if no db is passed, the database will not get updated and you 
   are working purely in memory."
  [{:keys [db tm alert-logfile]}]
  (let [{:keys [order-orderupdate-flow 
                send-new-order-to-flow]} (create-flows tm)
        order-manager (order-manager-start {:db db 
                                            :alert-logfile alert-logfile
                                            :order-orderupdate-flow order-orderupdate-flow})
        state {:db db
               :tm tm
               :order-manager order-manager
               :send-new-order-to-flow send-new-order-to-flow
               }]
     state))

(defn portfolio-manager-stop [{:keys [order-manager]}]
  (info "portfolio-manager stopping..")
  (order-manager-stop order-manager))

(defn get-working-orders [{:keys [order-manager]}]
  (om/get-working-orders order-manager))

(defn create-order [{:keys [tm send-new-order-to-flow] :as this}
                    {:keys [account asset side quantity type] :as order}]
  (if (s/validate-order order)
    (let [order-id (nano-id 8)
          order (assoc order
                       :order-id order-id
                       :date-created (t/inst))]
      (send-new-order-to-flow {:order order})
      (if tm
        (p/order-create! tm order)
        (error "cannot send order - :tm nil")))
    (do
      (error "order invalid error: " (s/human-error-order order))
      (throw (ex-info "order-invalid" {:order order
                                       :error (s/human-error-order order)})))))


(comment
  (nano-id 8)
 ; 
  )