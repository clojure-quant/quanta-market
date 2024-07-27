(ns quanta.market.broker.bybit.quotefeed
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.task.subscription :as s]
   [quanta.market.broker.bybit.msg.lasttrade :as lt]
   [quanta.market.broker.bybit.websocket :refer [create-websocket]]
   [quanta.market.util :refer [mix]]))


(defrecord bybit-feed [opts websocket]
  p/quotefeed
  (start-quote [this]
               (info "connecting bybit-quote websocket ")
               (p/start! websocket))
  (stop-quote [this]
              (info "stopping bybit-quote websocket ")
              (p/stop! websocket))
  (subscribe-last-trade! [this {:keys [asset]}]
     (s/subscription-start!
       (p/current-connection websocket)
       :asset/trade asset))
  (unsubscribe-last-trade! [this {:keys [asset]}]
     (s/subscription-stop!
       (p/current-connection websocket) :asset/trade asset))
  (last-trade-flow [this account-asset]
    (let [flow (p/msg-in-flow websocket)]
      (assert flow "missing msg-in-flow")
      (lt/last-trade-flow flow account-asset)))
   (msg-flow [this]
            (let [msg-in (p/msg-in-flow websocket)
                  msg-out (p/msg-out-flow websocket)]
              (assert msg-in "msg-in flow nil")
              (assert msg-out "msg-out flow nil")
              (mix msg-in msg-out ))))
 

(defmethod p/create-quotefeed :bybit
  [opts]
  (info "creating bybit quotefeed : " opts)
  (let [opts (merge {:mode :main
                     :segment :spot} opts)
        websocket (create-websocket opts)]
    (bybit-feed. opts websocket)))



  