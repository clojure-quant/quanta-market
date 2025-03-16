(ns demo.dev.order-bybit-trade
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [nano-id.core :refer [nano-id]]
   [quanta.market.util :refer [start-flow-logger! stop!]]
   [quanta.market.broker.bybit.trade] ; side effect
   [demo.env.accounts :refer [accounts-trade]]))

accounts-trade

(def opts (get accounts-trade :rene/test4))

opts

(def bb (p/create-tradeaccount opts))

bb

(start-flow-logger!
 ".data/bybit-account2.txt"
 :account
 (p/account-flow bb))

(start-flow-logger!
 ".data/bybit-account-msg2.txt"
 :account-msg
 (p/account-msg-flow bb))

(def order-spot-market-buy
  {:order-id (nano-id 8)
   :asset "BTCUSDT.S"
   :side :buy
   :qty 0.002
   :ordertype :market})

(def order-spot-market-sell
  {:order-id (nano-id 8)
   :asset "BTCUSDT.S"
   :side :sell
   :qty 0.002
   :ordertype :market})

(m/? (p/order-create! bb order-spot-market-buy))

(m/? (p/order-create! bb order-spot-market-sell))

