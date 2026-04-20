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
; "Elapsed time: 131024.045292 msecs"
; {:success 1908, :error 0, :error-details []}

(time
 (m/? (import-splits-list
       ctx
       (assoc base :list "equity-20mio"))))
; "Elapsed time: 195041.628529 msecs"
{:success 1908, :error 0, :error-details []}

(m/? (get-splits (:ss system) "IGE"))
(m/? (get-splits (:ss system) "MSFT"))
(m/? (get-splits (:ss system) "SLV"))
(m/? (delete-splits (:ss system) "SLV"))