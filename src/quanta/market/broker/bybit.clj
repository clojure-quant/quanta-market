(ns quanta.market.broker.bybit
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.connection :as c]
   [quanta.market.broker.bybit.task.auth :as a]
   [quanta.market.broker.bybit.task.order :as o]))

(defrecord bybit [opts conn]
  ;
  p/connection
  (start! [this opts]
    (let [conn (c/connection-start! opts)
          ;publish-quote! (fn [fix-msg]
          ;                 (let [quote (fix-quote->quote fix-msg)]
          ;                   (publish! this quote)))
          ]
      (reset! (:conn this) conn)
      ;(fix-api/on-quote client publish-quote!)
      (when-let [creds (:creds opts)]
        (info "authenticating secure account..")
        (m/? (a/authenticate! conn creds)))
      conn))
  (stop! [this opts]
    (let [{:keys [client] :as state} @(:state @this)]
      (c/connection-stop! client)
      (reset! (:conn this) nil)
      nil))
  ;
  p/trade
  (order-create! [this order]
    (o/order-create! @(:conn this) order))
  (order-cancel! [this order]
    (o/order-cancel! @(:conn this) order))
  ;(quote-stream [this]
  ;  (get-stream this))
  )


(defmethod p/create-account :bybit
  [opts]
  (info "creating bybit : " opts)
  (bybit. opts (atom nil)))
  