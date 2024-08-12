(ns quanta.market.robot.exit.signal
  (:require
   [missionary.core :as m]
   [quanta.market.robot.exit.time :refer [get-exit-time time-trigger]]
   [quanta.market.robot.exit.price :refer [profit-trigger loss-trigger]]))

(defn exit-signal
  "returns a missionary task.
   task will eventually return either of :time :profit :loss"
  [algo-opts position]
  (let [exit-time (get-exit-time algo-opts (:entry-date position))
        exit-tasks (->> [(profit-trigger algo-opts position)
                         (loss-trigger algo-opts position)
                         (when exit-time
                           (time-trigger exit-time))]
                        (remove nil?))]
    (apply m/race exit-tasks)))

(comment

  (require '[tick.core :as t])
  (def algo-opts {:calendar [:crypto :m]
                  :exit [:loss-percent 5.0
                         :profit-percent 10.0
                         :time 2]})
  (def position {:asset "BTCUSDT"
                 :side :long
                 :entry-date (t/instant)
                 :entry-price 10000.0
                 :qty 0.1})

  (m/? (exit-signal algo-opts position))
     ;; => :time or :profit or :loss

     ;; this one has a position that is older and older, so
     ;; it might be that this task returns immediately, because
     ;; the current time is already below the time of the exit-date 

  (m/? (exit-signal algo-opts
                    (assoc position :entry-date (t/instant))))

     ;; => :time

 ; 
  )