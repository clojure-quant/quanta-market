(ns quanta.market.adapter.eod-close-time
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [tech.v3.datatype :as dtype]
   [ta.calendar.calendars :refer [get-calendar]]))

(defn date->zoned
  "returns the date as instant set to the day close in EST
   NOTE: expecting here that the calendar has EST as timezone."
  [dt cal-close-time cal-timezone]
  (-> (t/at dt cal-close-time)
      (t/in cal-timezone)
      (t/instant)))

(defn adjust-time-to-exchange-close [bar-ds calendar-kw]
  (let [{:keys [close timezone]} (get-calendar calendar-kw)
        date-vec (:date bar-ds)
        date-time-vec  (dtype/emap #(date->zoned % close timezone)
                                   :instant
                                   date-vec)]
    (tc/add-or-replace-column bar-ds :date date-time-vec)))
