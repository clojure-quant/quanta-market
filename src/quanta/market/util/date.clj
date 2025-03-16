(ns quanta.market.util.date
  (:require
   [clojure.edn]
   [tick.core :as t]
   ;[tick.timezone]
   ;[tick.locale-en-us]
   [cljc.java-time.instant :as ti]
   [cljc.java-time.local-date :as ld]
   [cljc.java-time.local-date-time :as ldt]
   [cljc.java-time.zoned-date-time :as zdt]
   [cljc.java-time.zone-offset :refer [utc]]
   [cljc.java-time.format.date-time-formatter :as fmt :refer [of-pattern
                                                              ;iso-date
                                                              ]]))

; #time/date       java.time.LocalDate
; #time/date-time  java.time.LocalDateTime
; #time/instant    java.time.Instant (milliseconds)

;; parsing

(def date-fmt (of-pattern "yyyy-MM-dd"))
(def datetime-fmt (of-pattern "yyyy-MM-dd HH:mm:ss"))

;(def row-date-format-
;  (fmtick/formatter "yyyy-MM-dd")) ; 2019-08-09

(defn parse-date-only [s]
  (try
    (-> s
        (ld/parse date-fmt))
    (catch Exception _
      nil)))

(defn parse-date [s]
  (try
    (-> s
        (ld/parse date-fmt)
        (t/at  (t/time "00:00:00")))
    (catch Exception _
      nil)))

(defn parse-datetime [s]
  (try
    (ldt/parse s datetime-fmt)
    (catch Exception _
      nil)))

(defn fmt-yyyymmdd [dt]
  (if dt
    (t/format (t/formatter "YYYY-MM-dd")  (t/zoned-date-time dt))
    ""))

; *****************************************************************************
(comment

; parse
  (parse-date "2021-06-05")
  (parse-datetime "2021-06-05 11:30:01")

  (=   (parse-date "2021-06-05")  (parse-date "2021-06-05"))
  (=   (parse-date "2021-06-05")   (parse-date "2021-06-06"))

  (=   (parse-datetime "2021-06-05 12:30:01")   (parse-datetime "2021-06-05 12:30:01"))
  (=   (parse-datetime "2021-06-05 12:30:01")   (parse-datetime "2021-06-05 12:30:02"))

;
  )
