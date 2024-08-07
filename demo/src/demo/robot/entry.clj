(ns demo.robot.entry
  (:require
   [missionary.core :as m]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [flow-sender start-logging mix current-v]]
   [quanta.market.robot.position :refer [start-entry-robot]]
   [demo.env :refer [qm pm]]))

(def signal-flow-sender (flow-sender))

(defn send-signal [signal]
  (warn "new signal: " signal)
  ((:send signal-flow-sender) signal))

(def robot (start-entry-robot 
            {:qm qm 
             :pm pm}
            {:account :rene/test4
             :qty 0.001
             :feed :bybit
             :diff 0.001}
            (:flow signal-flow-sender)
            ".data/robot-entry.txt"))

(comment 
   (send-signal {:asset "BTCUSDT"
                 :side :buy})
  
   (send-signal {:asset "BTCUSDT"
                :side :sell})
  
  
 ; 
  )
