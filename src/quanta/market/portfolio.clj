(ns quanta.market.portfolio
  (:require
   [missionary.core :as m]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [nano-id.core :refer [nano-id]]
   [tick.core :as t]
   [quanta.market.util :refer [flow-sender start-logging mix]]
   [quanta.market.protocol :as p]
   [quanta.market.trade.schema :as s]
   [quanta.market.trade.db :refer [store-new-order! store-order-update!]]
   [quanta.market.trade.order-status :as order-status]))




(defn wrap-order-update-flow [f]
  (m/eduction (map (fn [r] {:order-update r})) f))

(defn wrap-order-flow [f]
  (m/eduction (map (fn [r] {:order r})) f))


(defn portfolio-manager-start
  "starts the porfolio manager.
   db is optional. if no db is passed, the database will not get updated and you 
   are working purely in memory."
  [{:keys [db tm alert-logfile]}]
  (let [send-new-order-to-flow (fn [order]
                         (warn "creating new order in db: " order)
                         ((:send order-create-flow-sender) order))
        wrapped-orderupdate-flow (wrap-order-update-flow 
                                    (or order-update-flow
                                        (p/order-update-flow tm)))
        order-flow (:flow order-create-flow-sender)
        state {:db db
               :tm tm
               :order-update-flow (mix wrapped-orderupdate-flow order-flow)
               ;:open-positions open-positions
               :send-new-order-to-flow send-new-order-to-flow
               }
        update-task (create-update-task state)
        stop-update-processor (update-task #(info "order-update-processor stopped successfully: " %)
                                           #(error "order-update-processor crashed: " %))]
    (when alert-logfile
      (start-logging alert-logfile alert-flow))
    (assoc state
           :stop-update-processor stop-update-processor)))

(defn portfolio-manager-stop [{:keys [stop-update-processor]}]
  (info "portfolio-manager stopping..")
  (stop-update-processor))


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