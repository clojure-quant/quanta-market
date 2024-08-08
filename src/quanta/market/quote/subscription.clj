(ns quanta.market.quote.subscription
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.util :as u]
   [quanta.market.protocol :as p])
  (:import [missionary Cancelled]))


(defn subscribing-unsubscribing-quote-flow [{:keys [feed lock subscriptions] :as this}  sub]
  (let [topic-f (p/topic-view feed sub)
        c (p/get-conn feed) ; this returns the websocket.
        conn-f (p/connection-flow c)]
    (m/stream
     (m/ap
      (debug "get-quote will start a new subscription..")
      (let [conn (m/?> conn-f)
            _ (info "quote subscriber " sub " new connection: " conn)]
        (when conn ; (and conn (not (reduced? conn)))
          (if (= conn :stop)
            (do (info "connection stopped.")
                ;(m/holding lock
                (u/with-lock lock 
                          (swap! subscriptions dissoc sub)))
            (do (info "got a new connection .. starting subscription.")
                (m/? (p/subscription-start! feed conn sub))
                (try
                  (m/?> topic-f)
                  (catch Cancelled _
                     (info "quote subscriber " sub " cancelled.")        
                     (m/?  (m/compel  (p/subscription-stop! feed conn sub)))
                  ))))
        ))))))

(defrecord topic-subscriber [lock subscriptions feed]
  p/subscription-topic
  (get-feed [this]
    feed)
  (get-topic [this topic]
    (or (get @subscriptions topic)
        ;(m/holding lock
        (u/with-lock lock 
                   (let [qs (subscribing-unsubscribing-quote-flow this topic)]
                     (swap! subscriptions assoc topic qs)
                     qs)))))

(defn create-topic-subscriber [feed]
  (let [subscriptions (atom {})
        ;lock (m/sem)
        lock (u/rlock)
        ]
    (topic-subscriber. lock subscriptions feed)))



