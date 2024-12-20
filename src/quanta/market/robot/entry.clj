(ns quanta.market.robot.entry
  (:require
   [missionary.core :as m]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.util :refer [start-logging mix wrap-blk]]
   [quanta.market.protocol :as p]
   [quanta.market.robot.order :refer [place-order-near-market]]))

(defn create-entry-f [{:keys [qm pm] :as env}
                      {:keys [account qty feed diff] :as robot-opts}
                      signal-f]
  (assert qm "entry-robot needs :qm")
  (assert pm "entry-robot needs :pm")
  (assert account "entry-robot needs :account")
  (assert qty "entry-robot needs :qty")
  (assert feed "entry-robot needs :feed")
  (assert diff "entry-robot needs :diff")
  (m/ap
   (let [signal (m/?> signal-f)
         {:keys [asset side]} signal
         order-feed-diff (merge robot-opts signal)
         place-order-t (place-order-near-market env order-feed-diff)
         ;place-order-t (wrap-blk place-order-t) ; this does not change anything.
         order (m/? place-order-t)]
     (warn "order placed: " order)
     {:signal signal
      :order-feed-diff order-feed-diff
      :order order})))

(defn start-entry-robot [{:keys [qm pm] :as env}
                         {:keys [account qty feed diff] :as robot-opts}
                         signal-f
                         logfile]
  (let [entry-f (create-entry-f env robot-opts signal-f)
        mixed-f (mix signal-f entry-f)]
    (start-logging logfile mixed-f)))

; new signal
; log new signal
; can open order ?
; log can-open 
; create limit near market.
; -> result: failed-to-open or position-opened.

(defn ExitPosition [algo-opts position]
    ;(M/ap (m/?> 
  )

