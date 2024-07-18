(ns quanta.market.broker.bybit
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.connection :as c]
   [quanta.market.broker.bybit.task.auth :as a]
   [quanta.market.broker.bybit.task.order :as o]
   [quanta.market.broker.bybit.pinger :as pinger]))

(defrecord bybit [opts conn ping]
  ;
  p/connection
  (start! [this opts]
    (let [conn (c/connection-start! opts)]
      (reset! (:conn this) conn)
      (when-let [creds (:creds opts)]
        (info "authenticating secure account..")
        (m/? (a/authenticate! conn creds)))
      (pinger/start-pinger conn ping)
      conn))
  (stop! [this opts]
    (let [conn @(:conn this)]
      (pinger/stop-pinger (:ping this))
      (when conn
        (info "stopping connection..")
        (c/connection-stop! conn)
        (reset! (:conn this) nil))
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
  (bybit. opts (atom nil) (atom nil)))

  