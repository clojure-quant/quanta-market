

(defprotocol quotefeed
  (start-quote [this])
  (stop-quote [this])
  (subscribe-last-trade! [this sub])
  (unsubscribe-last-trade! [this unsub])
  (last-trade-flow [this account-asset])
  (msg-flow-quote [this]))