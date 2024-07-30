(ns quanta.market.broker.bybit.quotefeed-category
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.task.subscription :as s]
   [quanta.market.broker.bybit.websocket2 :refer [create-websocket2]]
   [quanta.market.broker.bybit.topic :refer [format-topic-sub topic-data-flow topic-transformed-flow]]
   [quanta.market.util :refer [mix] :as util])
  (:import [missionary Cancelled]))


(defn subscribing-unsubscribing-quote-flow [{:keys [websocket lock subscriptions] :as this}  sub]
  (let [topic (format-topic-sub sub)
        msg-in (p/msg-in-flow websocket)
        topic-data-f (topic-data-flow msg-in topic)
        topic-f (topic-transformed-flow topic-data-f sub)
        conn-f (p/current-connection websocket)]
    (util/cont
     (m/ap
      (debug "get-quote will start a new subscription..")
      (let [conn (m/?> conn-f)
            _ (info "quote subscriber " sub " new connection: " conn)]
        ;(m/amb "listening to data")
        (when conn 
           (m/? (s/subscription-start! conn topic)))
        (try
          (m/amb (m/?> topic-f))
          (catch Cancelled _
            (info "quote subscriber " sub " cancelled.")
            (do  (when conn 
                   (debug "get-quote will stop an existing subscription..")
                   (m/?  (m/compel  (s/subscription-stop! conn topic))))
                 (debug "get-quote has unsubscribed. now removing from atom..")
                 (m/holding lock
                            (swap! subscriptions dissoc sub))))))))))



(defrecord bybit-category-feed [opts websocket subscriptions lock]
  p/subscription-topic
  (get-topic [this sub]
    (or (get @subscriptions sub)
        (m/holding lock
                   (let [qs (subscribing-unsubscribing-quote-flow this sub)]
                     (swap! subscriptions assoc sub qs)
                     qs)))))


(defmethod p/create-quotefeed :bybit-category
  [opts]
  (info "wiring up bybit-category feed : " opts)
  (let [opts (merge {:mode :main
                     :segment :spot} opts)
        websocket (create-websocket2 opts)
        subscriptions (atom {})
        lock (m/sem)]
    (bybit-category-feed. opts websocket subscriptions lock)))



