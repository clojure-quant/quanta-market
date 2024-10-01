(ns quanta.market.broker.paper
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.connection :as c]
   [quanta.market.broker.bybit.task.order :as o]))

(defrecord paper [opts conn]
  p/trade
  (start [this]
    (let [conn (c/connection-start! (:opts this) :paper)
          ;publish-quote! (fn [fix-msg]
          ;                 (let [quote (fix-quote->quote fix-msg)]
          ;                   (publish! this quote)))
          ]
      (reset! (:conn this) conn)
      ;(fix-api/on-quote client publish-quote!)
      ))
  (stop [this]
    (let [{:keys [client] :as state} @(:state @this)]
      (c/connection-stop! client)
      (reset! (:conn this) nil)))
  (order-create! [this order]
    (o/order-create! @(:conn this) order))
  (order-cancel! [this order]
    (o/cancel! @(:conn this) order))
  ;(quote-stream [this]
  ;  (get-stream this))
  (subscription-start! [this asset]
    (generate-quotes asset)))

(defmethod p/create-account :paper
  [opts]
  (info "creating paper : " opts)
  (paper. opts (atom nil)))


