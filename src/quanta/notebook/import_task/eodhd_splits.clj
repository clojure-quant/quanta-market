(ns quanta.notebook.import-task.eodhd-splits
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [tablecloth.api :as tc]
   [quanta.bar.split.service :refer [get-splits delete-splits]]
   [quanta.recipy.eodhd-import-splits-list :refer [import-splits-list]]
   [modular.system :refer [system]]))

(def ctx (:ctx system))

(def base 
  {:start (t/date "1980-01-01")
   :end (t/date "2026-04-19")})

(m/? (import-splits-list
      ctx
      (assoc base :list "flo")))

(time
 (m/? (import-splits-list
       ctx
       (assoc base :list "etf-10mio"))))


; {:asset "XLY", :start #time/date "1980-01-01", :end #time/date "2026-04-19"}
; "Elapsed time: 58455.765072 msecs"
;{:success 947, :error 0, :error-details []}
; 530 splits
; "Elapsed time: 131024.045292 msecs"
{:success 1908, :error 0, :error-details []}

(time
 (m/? (import-splits-list
       ctx
       (assoc base :list "equity-20mio"))))
; "Elapsed time: 115999.964061 msecs"
{:success 1000, :error 0, :error-details []}
; "Elapsed time: 195041.628529 msecs"
{:success 1908, :error 0, :error-details []}
; "Elapsed time: 342120.966022 msecs"
{:success 4019, :error 0, :error-details []}

{:asset "JNJ", :start #time/date "1980-01-01", :end #time/date "2026-04-19"}

(m/? (get-splits (:ss system) "IGE"))
(m/? (get-splits (:ss system) "MSFT"))


(m/? (get-splits (:ss system) "SLV"))

(m/? (delete-splits (:ss system) "SLV"))