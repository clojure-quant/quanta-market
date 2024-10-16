(ns dev.barimport.bybit-parallel
  (:require
   [tick.core :as t]
   [clojure.pprint :refer [print-table]]
   [quanta.market.barimport.bybit.import-parallel :as bb]))

(def calendar [:crypto :m])

(def window-feb-29
  {:start (-> "2024-02-29T00:00:00Z" t/instant)
   :end (-> "2024-02-29T00:07:00Z" t/instant)})

(bb/partition-requests calendar window-feb-29)
;; => ()
;; problem: why is there no february 29 in 2024??

(def window
  {:start (-> "2024-05-01T00:00:00Z" t/instant)
   :end (-> "2024-05-02T00:00:00Z" t/instant)})
;; => #'dev.barimport.bybit-parallel/window

(-> (bb/partition-requests calendar window)
    print-table)
;; => nil
;; |               :start |                           :end |
;; |----------------------+--------------------------------|
;; | 2024-05-01T09:01:00Z | 2024-05-01T23:59:59.999999999Z |

;; problem: why does the time begin at 9:01

(require '[ta.calendar.core :refer [fixed-window]])

(-> (fixed-window calendar window)
    last)
;; => #time/zoned-date-time "2024-05-01T00:01Z[UTC]"

(-> (fixed-window calendar window)
    first)
;; => #time/zoned-date-time "2024-05-01T23:59:59.999999999Z[UTC]"

(-> (fixed-window calendar window)
    count)
;; => 1440

(-> (fixed-window calendar window)
    bb/req-window)

;; => {:start #time/instant "2024-05-01T00:01:00Z", 
;;     :end   #time/instant "2024-05-01T23:59:59.999999999Z"}

(bb/parallel-requests
 {:asset "BTCUSDT"
  :calendar calendar}
 window)



