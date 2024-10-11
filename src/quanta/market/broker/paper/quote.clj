(ns quanta.market.broker.paper.quote
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [quanta.market.protocol :as p])
  (:import [missionary Cancelled]))

(defn initial-price []
  (rand 10000)
  ;10000.0
  )

(defn update-price [p]
  (let [i (rand-int 5)]
    (case i
      0 (/ p 1.03) ; strong down
      1 (/ p 1.01) ; weak down
      2  p ; unchanged
      3 (* p 1.01) ; weak up
      4 (* p 1.03) ; strong up
      )))

(defn generate-quotes [topic]
  (let [asset (:asset topic)]
    (m/stream
     (m/ap
   ; startup
      (println "start generating quotes for: " asset)
      (loop [p (initial-price)]
        (m/amb {:asset asset
                :bid p
                :ask p
                :last p
                :date (t/instant)
                :feed :random}
               (let [recur? (try
                              (m/? (m/sleep 5000 true))
                              (catch Cancelled ex
                             ; shutdown
                                (println "stop generating quotes for: " asset)
                                false))]
                 (when recur?
                   (recur (update-price p))))))))))

(defrecord random-feed [lock subscriptions]
  p/subscription-topic
  (get-topic [this topic]
    (or (get @subscriptions topic)
        (m/holding lock
                   (let [qs (generate-quotes topic)]
                     (swap! subscriptions assoc topic qs)
                     qs)))))

(defmethod p/create-quotefeed :random
  [opts]
  (let [subscriptions (atom {})
        lock (m/sem)]
    (random-feed. lock subscriptions)))





