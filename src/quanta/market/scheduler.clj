(ns quanta.market.scheduler
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [ta.calendar.core :refer [current-close next-close]]
   [ta.calendar.calendars :refer [get-calendar-list]]
   [ta.calendar.interval :refer [get-interval-list]]))

(defn scheduler
  "returns a missionary flow
   fires all upcoming timestamps for a calendar
   "
  [calendar]
  (m/stream
   (m/ap
    (println "starting scheduler for calendar: " calendar)
    (let [[market-kw interval-kw] calendar]
      (loop [dt (t/now)
             current-dt (current-close market-kw interval-kw dt)]
        (let [current-dt-inst (t/instant current-dt)
              diff-ms (* 1000 (- (t/long current-dt-inst) (t/long dt)))]
          (when (> diff-ms 0)
           ;(println "sleeping for ms: " diff-ms " until: " current-dt)
            (m/? (m/sleep diff-ms current-dt))
           ;(println "finished sleeping")
            :bongo)
          (m/amb
           current-dt
           (recur (t/now)
                  (next-close market-kw interval-kw current-dt)))))))))

(defn all-calendars []
  (->> (for [c (get-calendar-list)
             i (get-interval-list)]
         (let [cal [c i]]
           [cal (scheduler cal)]))
       (into {})))

(def calendar-dict (all-calendars))

(defn get-calendar-flow [calendar]
  (get calendar-dict calendar))






