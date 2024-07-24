(ns quanta.market.trade.transactor
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.util :refer [flow-sender start-logging mix]]
   [quanta.market.trade.schema :as s]
   [quanta.market.trade.order :refer [order-change-flow
                                      trade-flow]]
   [quanta.market.trade.position :refer [position-change-flow
                                         open-positions-flow]]))



(defn transactor-start
  "starts the transactor.
   the transactor keeps working-orders and open-positions from
   a order-orderupdate-flow.

   db is optional. if no db is passed, the database will not get updated and you 
   are working purely in memory."
  [{:keys [order-orderupdate-flow logfile db]}]
  ; load-working-orders from db.
  (let [; flows
        order-change-f (order-change-flow order-orderupdate-flow)
        trade-f (trade-flow order-change-f)
        ;working-order-f (current-working-orders order-orderupdate-flow)
        ;position-change-f (position-change-flow trade-f)
        ;open-position-f (open-positions-flow position-change-f)
        ; log
        logger-dispose! (if logfile
                          (let [log-flow (mix trade-f ;open-position-f
                                              order-change-f
                                              )]
                            (info "transactor is logging to: " logfile)
                            (start-logging logfile log-flow))
                          (warn "order-manager is NOT LOGGING!"))


        state {:db db
              ; :working-orders working-orders
               ;:order-orderupdate-flow order-orderupdate-flow
               :logger-dispose!! logger-dispose!}
        ;update-task (create-update-task state)
        ;stop-update-processor (update-task #(info "order-update-processor stopped successfully: " %)
        ;                                   #(error "order-update-processor crashed: " %))
        ]
    ;(assoc state :stop-update-processor stop-update-processor)
    state
    ))


(defn transactor-stop [{:keys [stop-update-processor logger-dispose!]}]
  (info "transactor stopping..")
  ;(stop-update-processor)
  (when logger-dispose!
    (logger-dispose!)))