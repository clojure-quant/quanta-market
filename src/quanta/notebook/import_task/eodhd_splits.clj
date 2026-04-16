(ns demo.import-task.eodhd-splits
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [tablecloth.api :as tc]
   [quanta.bar.db.nippy :refer [save-ds]]
   [quanta.bar.split.service :refer [save-splits delete-splits get-splits]]
   [quanta.recipy.eodhd-import-splits-list :refer [import-splits-list]]
   [demo.env-bar :refer [eodhd eodhd-token bardb-nippy ctx]]))

(m/? (import-splits-list
      ctx
      {:list "flo"
       :start (t/date "1980-01-01")
       :end (t/date "2026-03-20")}))

(time
 (m/? (import-splits-list
       ctx
       {:list "etf-10mio"
        :start (t/date "1980-01-01")
        :end (t/date "2026-03-20")})))
; "Elapsed time: 58455.765072 msecs"
;{:success 947, :error 0, :error-details []}
; 530 splits
; "Elapsed time: 131024.045292 msecs"
{:success 1908, :error 0, :error-details []}

(time
 (m/? (import-splits-list
       ctx
       {:list "equity-20mio"
        :calendar [:us :d]
        :start (t/date "1980-01-01")
        :end (t/date "2026-03-20")})))
; "Elapsed time: 115999.964061 msecs"
{:success 1000, :error 0, :error-details []}
; "Elapsed time: 195041.628529 msecs"
{:success 1908, :error 0, :error-details []}
; "Elapsed time: 342120.966022 msecs"
{:success 4019, :error 0, :error-details []}

(m/? (get-splits (:ss ctx) "IGE"))
(m/? (get-splits (:ss ctx) "MSFT"))