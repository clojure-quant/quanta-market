(ns quanta.notebook.import-task.eodhd-bars
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [quanta.bar.protocol :refer [get-bars summary]]
   [quanta.recipy.eodhd-import-bars-list :refer [import-bars-list]]
   [modular.system :refer [system]]))

(def ctx (:ctx system))

(def base
  {:calendar [:us :d]
   :start (t/zoned-date-time "1980-01-01T00:00:00Z")
   :end (t/zoned-date-time "2026-04-19T00:00:00Z")})

(m/? (import-bars-list
      ctx
      (assoc base :list "flo")))
; 2

(time
 (m/? (import-bars-list
       ctx
       (assoc base :list "etf-10mio"))))
; "Elapsed time: 71530.718723 msecs"
{:success 947, :error 0, :error-details []}

(time
 (m/? (import-bars-list
       ctx
       (assoc base :list "equity-20mio"))))
; "Elapsed time: 115999.964061 msecs"
{:success 1000, :error 0, :error-details []}

(m/? (summary (:bar-db-duck system) {:calendar [:us :d]}))

(m/? (get-bars (:bar-db-duck system) {:asset "A" :calendar [:us :d]} {}))
(m/? (get-bars (:bar-db-duck system) {:asset "SPY" :calendar [:us :d]} {}))
