(ns quanta.market.quote.subscription
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [mix] :as util])
  (:import [missionary Cancelled]))


(defn subscribing-unsubscribing-quote-flow [{:keys [lock subscriptions feed]} topic]
  (let [msg-in-f (p/msg-in-flow feed)
        topic-f (p/topic-view feed msg-in-f topic)
        conn-f (p/current-connection feed)]
    (util/cont
     (m/ap
      (debug "get-quote will start a new subscription..")
      (let [conn (m/?> conn-f)
            _ (info "quote subscriber " topic " new connection: " conn)]
        ;(m/amb "listening to data")
        (when conn 
           (m/? (p/subscription-start! feed conn topic)))
        (try
          (m/amb (m/?> topic-f))
          (catch Cancelled _
            (info "quote subscriber " topic " cancelled.")
            (do  (when conn 
                   (debug "get-quote will stop an existing subscription..")
                   (m/?  (m/compel  (p/subscription-stop! feed conn topic))))
                 (debug "get-quote has unsubscribed. now removing from atom..")
                 (m/holding lock
                            (swap! subscriptions dissoc topic))))))))))



(defrecord topic-subscriber [lock subscriptions feed]
  p/subscription-topic
  (get-topic [this topic]
    (or (get @subscriptions topic)
        (m/holding lock
                   (let [qs (subscribing-unsubscribing-quote-flow this topic)]
                     (swap! subscriptions assoc topic qs)
                     qs)))))


(defn create-topic-subscriber [feed]
  (let [subscriptions (atom {})
        lock (m/sem)]
    (topic-subscriber. lock subscriptions feed)))



