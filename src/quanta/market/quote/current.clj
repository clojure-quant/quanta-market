(ns quanta.market.quote.current
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p])
  (:import [missionary Cancelled]))

(defonce subscriptions (atom {}))

(def lock (m/sem))

(defn subscribing-unsubscribing-quote-flow [qm sub]
  (let [q (p/last-trade-flow qm sub)]
    (m/? (p/subscribe-last-trade! qm sub))
    (m/ap (try 
            (m/amb (m/?> q))
            (catch Cancelled _
                   (m/? (p/unsubscribe-last-trade! qm sub))
                   (m/holding lock
                       (swap! subscriptions dissoc sub)))))))

(defn get-quote [qm sub]
  (or (get @subscriptions sub)
      (m/holding lock
        (let [qs (subscribing-unsubscribing-quote-flow qm sub)]
           (swap! subscriptions assoc sub qs)  
           qs))))