(ns quanta.market.quote.current
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :as util])
  (:import [missionary Cancelled]))

(defonce subscriptions (atom {}))

(def lock (m/sem))

(defn subscribing-unsubscribing-quote-flow [qm sub]
  (info "get-quote will start a new subscription..")
  (let [q (p/last-trade-flow qm sub)]
    (m/? (p/subscribe-last-trade! qm sub))
    (util/cont
     (m/ap (try
             (m/amb (m/?> q))
             (catch Cancelled _
               (do  (info "get-quote will stop an existing subscription..")
                    (m/? (p/unsubscribe-last-trade! qm sub))
                    (info "get-quote has unsubscribed. now removing from atom..")
                    (m/holding lock
                               (swap! subscriptions dissoc sub)))))))))

(defn get-quote [qm sub]
  (or (get @subscriptions sub)
      (m/holding lock
                 (let [qs (subscribing-unsubscribing-quote-flow qm sub)]
                   (swap! subscriptions assoc sub qs)
                   qs))))

(comment
  @subscriptions
  (reset! subscriptions {})

 ; 
  )


