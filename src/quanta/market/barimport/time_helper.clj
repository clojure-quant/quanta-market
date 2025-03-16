(ns quanta.market.barimport.time-helper
  (:require
   [tick.core :as t])
  (:import
   [java.time Instant ZonedDateTime]))

(defn instant->epoch-millisecond [dt]
  (-> dt
      (t/long)
      (* 1000)))

(defn to-zoned-date-time [dt tz]
  (cond
    (instance? Instant dt) (t/in dt tz)
    (instance? ZonedDateTime dt) dt))