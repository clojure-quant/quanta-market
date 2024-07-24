(ns quanta.market.trade.transactor
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [crockery.core :as crockery]
   [quanta.market.util :refer [flow-sender start-logging mix]]
   [quanta.market.trade.schema :as s]
   [quanta.market.trade.order :refer [order-change-flow
                                      working-orders-flow
                                      order-dict->order-seq-flow
                                      trade-flow]]
   [quanta.market.trade.position :refer [position-change-flow
                                         open-positions-flow]]))

(defn wrap-title [title flow]
  (m/eduction (map (fn [data]
                     (str "\r\n" title "\r\n"
                          (if (string? data)
                              data
                            (pr-str data)
                            ))
                     )) flow))

(defn working-orders-table [working-orders]
   (with-out-str 
    (crockery/print-table
     [{:name :order-id, :align :left}
      {:name :asset, :align :right :title "asset" :key-fn #(get-in % [:open-order :asset])}
      {:name :asset, :align :right :title "side" :key-fn #(get-in % [:open-order :side])}
      {:name :asset, :align :right :title "qty" :key-fn #(get-in % [:open-order :qty])}
      {:name :asset, :align :right :title "fill-qty" :key-fn #(get-in % [:order-status :fill-qty])}
      {:name :asset, :align :right :title "fill-value" :key-fn #(get-in % [:order-status :fill-value])}]
     working-orders)))

(defn wrap-table [flow]
  (m/eduction (map working-orders-table) flow))

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
        working-order-dict-f (working-orders-flow order-change-f)
        working-order-f (order-dict->order-seq-flow working-order-dict-f)
        working-order-table-f (wrap-table working-order-f)
        trade-f (trade-flow order-change-f)
        ;position-change-f (position-change-flow trade-f)
        ;open-position-f (open-positions-flow position-change-f)
        ; log
        logger-dispose! (if logfile
                          (let [log-flow (mix (wrap-title "trade" trade-f) ;open-position-f
                                              (wrap-title "order-change" order-change-f)
                                              (wrap-title "working-orders" working-order-f)
                                               (wrap-title "working-orders-table" working-order-table-f)
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