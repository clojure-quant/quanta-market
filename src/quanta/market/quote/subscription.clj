(ns quanta.market.quote.subscription
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p])
  (:import [missionary Cancelled]))


(defn subscribing-unsubscribing-quote-flow [{:keys [feed lock subscriptions] :as this}  sub]
  (let [topic-f (p/topic-view feed sub)
        c (p/get-conn feed)
        conn-f (p/current-connection c)]
    (m/stream
     (m/ap
      (debug "get-quote will start a new subscription..")
      (let [conn (m/?> conn-f)
            _ (info "quote subscriber " sub " new connection: " conn)]
        ;(m/amb :connected)
        (when (and conn (not (reduced? conn)))
          (m/? (p/subscription-start! feed conn sub)))
        (try
          (let [topic (m/?> topic-f)]
                ;(when topic (println "subscribed topic: " topic))
                ;(m/amb topic)
            topic)
          (catch Cancelled cancel
            (info "quote subscriber " sub " cancelled.")
            (do  (when (and conn (not (reduced? conn)))
                   (debug "get-quote will stop an existing subscription..")
                   (m/?  (m/compel  (p/subscription-stop! feed conn sub))))
                 (debug "get-quote has unsubscribed. now removing from atom..")
                 (m/holding lock
                            (swap! subscriptions dissoc sub))
                 ;(m/amb :disconnected)
                 (throw cancel)))))))))


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



