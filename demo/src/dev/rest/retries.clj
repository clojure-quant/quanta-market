(ns dev.rest.retries
  (:require
   [missionary.core :as m]
   [quanta.market.util.missionary :refer [backoff]]))

(def delays                          ;; Our backoff strategy :
  (->> 1000                          ;; first retry is delayed by 1 second
       (iterate (partial * 2))       ;; exponentially grow delay
       (take 5)))                    ;; give up after 5 retries

delays
;; => (1000 2000 4000 8000 16000)

(def request                         ;; A mock request to exercise the strategy.
  (m/sp                              ;; Failure odds are made pretty high to
   (prn :attempt)                   ;; simulate a terrible connectivity
   (if (zero? (rand-int 6))
     :success
     (throw (ex-info "failed." {:worth-retrying true})))))

(m/? (backoff request delays))
;; returns either :success or throws an exception.
; depending on random numbers it could be:

; :attempt
; :attempt
; :attempt
; #=> :success

;; or it could fail:
; :attempt
; :attempt
; :attempt
; :attempt
; :attempt
; :attempt
; Execution error (ExceptionInfo) at dev.barimport.retries/fn$cr13790-block-1 (REPL:17).
; failed.


