(ns quanta.market.robot.exit
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-logging]]
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

(defn position-flow-processor [env robot-opts open-position-f]
  (m/ap
   (let [position (m/?> open-position-f)]
     (println "position: " position)
     position
     )))

(defn start-exit-robot [{:keys [qm pm] :as env}
                        {:keys [account qty feed diff] :as robot-opts}
                        logfile]
  (let [exit-signal-f (position-flow-processor env robot-opts (p/open-position-f pm))]
    (start-logging logfile exit-signal-f)))