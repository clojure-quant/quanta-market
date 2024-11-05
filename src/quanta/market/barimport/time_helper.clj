(ns quanta.market.barimport.time-helper
  (:require
   [tick.core :as t] ; tick uses cljc.java-time
   [ta.helper.date :refer []]
   [ta.calendar.calendars :refer [calendars]]
   [quanta.calendar.core :refer [open->close-dt close->open-dt]])
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

(defn to-calendar-open-time
  "uses quanta calendar.
   use this only for crypto or window alignment"
  [dt [calendar-kw interval-kw]]
  (let [timezone (-> calendars calendar-kw :timezone)
        zdt (to-zoned-date-time dt timezone)]
    (->> zdt
         (close->open-dt [calendar-kw interval-kw])
         t/instant)))

(defn to-calendar-close-time
  "uses quanta calendar.
  use this only for crypto or window alignment"
  [dt [calendar-kw interval-kw]]
  (let [timezone (-> calendars calendar-kw :timezone)
        zdt (to-zoned-date-time dt timezone)]
    (->> zdt
         (open->close-dt [calendar-kw interval-kw])
         t/instant)))

(defn window->open-time [window calendar]
  (assoc window
         :start (to-calendar-open-time (:start window) calendar)
         :end (to-calendar-open-time (:end window) calendar)))

(defn to-bar-close [open-dt n unit]
  (t/>> open-dt (t/new-duration n unit)))