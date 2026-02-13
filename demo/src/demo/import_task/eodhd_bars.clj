(ns demo.import-task.eodhd-bars
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [quanta.recipy.eodhd-import-bars-list :refer [import-bars-list]]
   [demo.env-bar :refer [eodhd eodhd-token bardb-nippy ctx]]))

(m/? (import-bars-list
      ctx
      {:list "flo"
       :calendar [:us :d]
       :start (t/zoned-date-time "1980-01-01T00:00:00Z")
       :end (t/zoned-date-time "2026-03-20T00:00:00Z")}))
; 2

(time
 (m/? (import-bars-list
       ctx
       {:list "etf-10mio"
        :calendar [:us :d]
        :start (t/zoned-date-time "1980-01-01T00:00:00Z")
        :end (t/zoned-date-time "2026-03-20T00:00:00Z")})))
; "Elapsed time: 71530.718723 msecs"
{:success 947, :error 0, :error-details []}

(time
 (m/? (import-bars-list
       ctx
       {:list "equity-20mio"
        :calendar [:us :d]
        :start (t/zoned-date-time "1980-01-01T00:00:00Z")
        :end (t/zoned-date-time "2026-03-20T00:00:00Z")})))
; "Elapsed time: 115999.964061 msecs"
{:success 1000, :error 0, :error-details []}
