(ns demo.dev.calendar
  (:require
   [missionary.core :as m]
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
             (println "robot processing date: " dt))
           nil
           (scheduler [:crypto :m])))

  ;; this is the test if calendar date/time firing is correct.

(def multirobot
  (let [s (get-calendar-flow [:crypto :m])
        robot1 (m/reduce (fn [_ dt]
                           (println "robot-1 processing date: " dt))
                         nil
                         s)
        robot2 (m/reduce (fn [_ dt]
                           (println "robot-2 processing date: " dt))
                         nil
                         s)]
    (m/join vector robot1 robot2)))

  ;; this is the test to see if multiple consumers that consume the same
  ;; stream do actually get the date events.

(def dispose!
  (multirobot
   #(prn ::success %)
   #(prn ::crash %)))

(dispose!)

