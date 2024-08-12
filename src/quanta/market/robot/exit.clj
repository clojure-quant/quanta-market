(ns quanta.market.robot.exit
  (:require
   [missionary.core :as m]
   [quanta.market.robot.exit.time :refer [get-exit-time time-trigger]]
   [quanta.market.robot.exit.price :refer [profit-trigger loss-trigger]]))

(defn exit-signal
  "returns a missionary task.
   task will eventually return either of :time :profit :loss"
  [env algo-opts position]
  (let [exit-time (get-exit-time algo-opts (:entry-date position))
        exit-tasks (->> [(profit-trigger env algo-opts position)
                         (loss-trigger env algo-opts position)
                         (when exit-time
                           (time-trigger exit-time))]
                        (remove nil?))]
    (apply m/race exit-tasks)))
