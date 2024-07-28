(ns quanta.market.broker.bybit.quotefeed2
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.task.subscription :as s]
   [quanta.market.broker.bybit.msg.lasttrade :as lt]
   [quanta.market.broker.bybit.websocket2 :refer [create-websocket2]]
   [quanta.market.util :refer [mix] :as util])
  (:import [missionary Cancelled]))


(defn subscribe-last-trade! [conn {:keys [asset] :as sub}]
  (info "subscribing: " sub)
  (s/subscription-start!
   conn
   :asset/trade asset))

(defn unsubscribe-last-trade! [conn {:keys [asset] :as sub}]
  (info "unsubscribing: " sub)
  (s/subscription-stop!
   conn :asset/trade asset))

(defn last-trade-flow [websocket account-asset]
  (m/ap
   (info "last-trade-flow conn: " websocket)
   (let [flow (p/msg-in-flow websocket)]
     (assert flow "missing msg-in-flow")
     (lt/last-trade-flow flow account-asset))))

(defn subscribing-unsubscribing-quote-flow [{:keys [websocket lock subscriptions] :as this}  sub]
  (util/cont
   (m/ap
    (info "get-quote will start a new subscription..")
    (let [conn (m/?> (p/current-connection websocket))
          _ (info "quote subscriber new connection: " conn)
          ;q (last-trade-flow websocket sub)
          q (p/msg-in-flow websocket)
          ]
      (m/amb "listening to data")
      (m/? (subscribe-last-trade! conn sub))
      (try
        (m/amb (m/?> q))
        (catch Cancelled _
          (do  (info "get-quote will stop an existing subscription..")
               (m/?  (m/compel  (unsubscribe-last-trade! conn sub)))
               (info "get-quote has unsubscribed. now removing from atom..")
               (m/holding lock
                          (swap! subscriptions dissoc sub)))))))))


(defrecord bybit-feed2 [opts websocket subscriptions lock]
  p/quote
  (get-quote [this sub]
    (or (get @subscriptions sub)
        (m/holding lock
                   (let [qs (subscribing-unsubscribing-quote-flow this sub)]
                     (swap! subscriptions assoc sub qs)
                     qs)))))

(defmethod p/create-quotefeed :bybit2
  [opts]
  (info "creating bybit quotefeed : " opts)
  (let [opts (merge {:mode :main
                     :segment :spot} opts)
        websocket (create-websocket2 opts)
        subscriptions (atom {})
        lock (m/sem)]
    (bybit-feed2. opts websocket subscriptions lock)))



