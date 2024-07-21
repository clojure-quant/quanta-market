(ns quanta.market.portfolio
  (:require
   [missionary.core :as m]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [nano-id.core :refer [nano-id]]
   [tick.core :as t]
   [quanta.market.util :refer [flow-sender start-logging]]
   [quanta.market.protocol :as p]
   [quanta.market.trade.schema :as s]
   [quanta.market.trade.db :refer [store-new-order! store-order-update!]]
   [quanta.market.trade.order-status :as order-status]))

(defn get-working-orders [{:keys [working-orders]}]
  @working-orders)

(defn get-working-order [{:keys [working-orders]} order-id]
  (get @working-orders order-id))



(defn process-order-new [{:keys [db alert working-orders] :as state}
                         order-update]
  (if (= (:type order-update) :order/new)
    (let [{:keys [order order-status] :as order-state} (order-status/create-new-order order-update)]
      (swap! working-orders assoc (:order-id order-status) order-state)
      (when db
        (store-new-order! db order-state)))
    (alert order-update "order-update has no working-order.")))

(defn process-order-update2 [{:keys [db alert working-orders] :as state} order-state order-update]
  (let [new-order-status (order-status/update-existing-order-status order-state order-update)]
    (when db
      (store-order-update! db order-update new-order-status))
    (if (order-status/open? new-order-status)
      (swap! working-orders update-in [(:order-id new-order-status) :order-status] new-order-status)
      (swap! working-orders dissoc (:order-id new-order-status)))))


(defn create-update-task [{:keys [db alert working-orders open-positions order-update-flow] :as state}]
  (assert order-update-flow ":order-update-flow option missing")
  (m/reduce (fn [_r order-update]
              (when order-update ; ignore nil
                (if (s/validate-order-update order-update)
                  (let [{:keys [order order-status] :as order-state} (get-working-order state (:order-id order-update))]
                    (info "processing order-update: " order-update)
                    (if order
                      (process-order-update2 state order-state order-update)
                      (process-order-new state order-update)))
                  (alert order-update "order-update does not comply with schema")))
              nil)
            nil order-update-flow))


(defn portfolio-manager-start
  "starts the porfolio manager.
   db is optional. if no db is passed, the database will not get updated and you 
   are working purely in memory."
  [{:keys [db tm order-update-flow alert-logfile]}]
  ; load-working-orders from db.
  (let [working-orders (atom {})
        alert-flow-sender (flow-sender)
        ;open-positions (atom {})
        send-alert (:send alert-flow-sender)
        alert (fn [order-update reason]
                (warn "order-update alert: " reason " order-update: " order-update)
                (send-alert {:reason reason
                             :order-update order-update}))
        alert-flow (:flow alert-flow-sender)
        state {:db db
               :tm tm
               :working-orders working-orders
               :order-update-flow (or order-update-flow
                                      (p/order-update-flow tm))
               ;:open-positions open-positions
               :alert alert
               :alert-flow alert-flow}
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


(defn create-order [{:keys [tm] :as this}
                    {:keys [account asset side quantity type] :as order}]
  (if (s/validate-order order)
    (let [order-id (nano-id 8)
          order (assoc order
                       :order-id order-id
                       :date-created (t/inst))]
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