(ns quanta.market.broker.bybit.quotefeed2
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.task.subscription :as s]
   ;[quanta.market.broker.bybit.topic.lasttrade :as lt]
   [quanta.market.broker.bybit.websocket2 :refer [create-websocket2]]
   [quanta.market.broker.bybit.topic :refer [format-topic-sub topic-data-flow topic-transformed-flow]]
   [quanta.market.util :refer [mix] :as util])
  (:import [missionary Cancelled]))


(defn subscribing-unsubscribing-quote-flow [{:keys [websocket lock subscriptions] :as this}  sub]
  (util/cont
   (m/ap
    (info "get-quote will start a new subscription..")
    (let [topic (format-topic-sub sub)
          msg-in (p/msg-in-flow websocket)
          topic-data-f (topic-data-flow msg-in topic)
          topic-f (topic-transformed-flow topic-data-f sub)
          conn (m/?> (p/current-connection websocket))
          _ (info "quote subscriber new connection: " conn)]
      (m/amb "listening to data")
      (m/? (s/subscription-start! conn topic))
      (try
        (m/amb (m/?> topic-f))
        (catch Cancelled _
          (do  (info "get-quote will stop an existing subscription..")
               (m/?  (m/compel  (s/subscription-stop! conn topic)))
               (info "get-quote has unsubscribed. now removing from atom..")
               (m/holding lock
                          (swap! subscriptions dissoc sub)))))))))


(defrecord bybit-feed2 [opts websocket subscriptions lock]
  p/quote
  (get-topic [this sub]
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



