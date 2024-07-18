(ns quanta.market.broker.bybit
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.connection :as c]
   [quanta.market.broker.bybit.task.auth :as a]
   [quanta.market.broker.bybit.task.order :as o]
   [quanta.market.broker.bybit.task.ping :refer [ping!]]))


(defn ping-task [conn]
  (m/sp
   (info "ping task start (this is the real start)..")
   (loop [i 0]
     (m/? (m/sleep 1000))
     (m/? (ping! conn))
     (recur (inc i)))))

(defrecord bybit [opts conn ping]
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

      (info "creating ping task..")
      (let [pinger (ping-task conn)
            ping-stop (pinger #(info "pinger stopped successfully")
                              #(info "pinger crashed!"))]
        (reset! ping ping-stop))
      conn))
  (stop! [this opts]
    (let [ping-dispose @(:ping this)
          conn @(:conn this)]
      (when ping-dispose
        (info "stopping pinger..")
        (ping-dispose)
        (reset! (:ping this) nil))
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
  