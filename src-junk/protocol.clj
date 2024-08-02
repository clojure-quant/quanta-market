

(defprotocol quotefeed
  (start-quote [this])
  (stop-quote [this])
  (subscribe-last-trade! [this sub])
  (unsubscribe-last-trade! [this unsub])
  (last-trade-flow [this account-asset])
  (msg-flow-quote [this]))

(defprotocol tradeaccount
  (start-trade [this])
  (stop-trade [this])
  (msg-flow [this])
  (order-update-msg-flow [this])
  (order-update-flow  [this]))
