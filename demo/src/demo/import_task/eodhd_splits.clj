(ns demo.import-task.eodhd-splits
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [tablecloth.api :as tc]
   [quanta.bar.db.nippy :refer [save-ds]]
   [quanta.recipy.eodhd-import-splits-list :refer [import-splits-list splits-a]]
   [demo.env-bar :refer [eodhd eodhd-token bardb-nippy ctx]]))

(m/? (import-splits-list
      ctx
      {:list "flo"
       :start (t/date "1980-01-01")
       :end (t/date "2026-03-20")}))

splits-a

(time
 (m/? (import-splits-list
       ctx
       {:list "etf-10mio"
        :start (t/date "1980-01-01")
        :end (t/date "2026-03-20")})))
; "Elapsed time: 58455.765072 msecs"
;{:success 947, :error 0, :error-details []}
; 530 splits

(time
 (m/? (import-splits-list
       ctx
       {:list "equity-10mio"
        :calendar [:us :d]
        :start (t/date "1980-01-01")
        :end (t/date "2026-03-20")})))
; "Elapsed time: 115999.964061 msecs"
{:success 1000, :error 0, :error-details []}
; "Elapsed time: 148781.550519 msecs"
{:success 1000, :error 0, :error-details []}

(tc/row-count @splits-a)
; 2812 = 2300 stocks, 500 etf

(save-ds (str (System/getenv "QUANTASTORE") "./splits.nippy.gz") @splits-a)