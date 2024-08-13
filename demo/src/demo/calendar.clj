(ns demo.calendar
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.util :refer [flow-sender]]
   [quanta.market.scheduler :refer [scheduler 
                                    all-calendars 
                                    get-calendar-flow
                                    calendar-dict]]))



  (count (all-calendars))
  calendar-dict
  
  (get-calendar-flow [:crypto :h])
  
    ; one robot.
  (m/?
   (m/reduce (fn [_ dt]
               (log "robot processing date: " dt))
             nil
             (scheduler [:crypto :m])))
  
  (def multirobot
    (let [s (get-calendar-flow [:crypto :m])
          robot1 (m/reduce (fn [_ dt]
                             (log "robot-1 processing date: " dt))
                           nil
                           s)
          robot2 (m/reduce (fn [_ dt]
                             (log "robot-2 processing date: " dt))
                           nil
                           s)]
      (m/join vector robot1 robot2)))
  
  (def dispose!
    (multirobot
     #(prn ::success %)
     #(prn ::crash %)))
  
  (dispose!)

