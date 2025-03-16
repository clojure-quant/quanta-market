(ns demo.dev.qm
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop!]]
   [quanta.market.quote :refer [quote-manager-start]]
   [demo.accounts :refer [accounts-quote]]))

(def qm (quote-manager-start accounts-quote))

(comment

  ; start websocket connections
  (p/start-quote qm)

; log all messages (for testing)
  (start-flow-logger!
   ".data/quotes-msg.txt"
   :quote/msg
   (p/msg-flow-quote qm))

;;; manual sub/unsub/flow

  (def qsub {:account :bybit
             :asset "BTCUSDT"})

  (start-flow-logger!
   ".data/quotes-manual.txt"
   :quote/manual
   (p/last-trade-flow qm qsub))

;; subscribe / unsubscribe

  (m/? (p/subscribe-last-trade! qm qsub))

  (m/? (p/unsubscribe-last-trade! qm qsub))

  ;; stop logger
  (stop! :quote/manual)
  (stop! :quote/msg)

; stop websocket connections
  (p/stop-quote qm)

;
  )

