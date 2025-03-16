(ns demo.dev.order-bybit-individual
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [nano-id.core :refer [nano-id]]
   [quanta.market.util :refer [start-flow-logger! stop!]]
   [quanta.market.broker.bybit.trade-update :refer [create-trade-update-feed]]
   [quanta.market.broker.bybit.trade-action :refer [create-trade-action]]
   [demo.env.accounts :refer [accounts-trade]]))

accounts-trade

(def opts (get accounts-trade :rene/test4))

opts

(def bb-orderupdate (create-trade-update-feed opts))

(start-flow-logger!
 ".data/bybit-orderupdate-msg3.txt"
 :orderupdate-msg
 (p/orderupdate-msg-flow bb-orderupdate))

; log all messages (for testing)
(start-flow-logger!
 ".data/bybit-orderupdate3.txt"
 :orderupdate
 (p/orderupdate-flow bb-orderupdate))

(def bb-order (create-trade-action opts))

(start-flow-logger!
 ".data/bybit-order-msg2.txt"
 :order
 (p/trade-action-msg-flow bb-order))

(def order-spot-market-buy
  {:order-id (nano-id 8)
   :asset "BTCUSDT.S"
   :side :buy
   :qty 0.002
   :ordertype :market})

(m/? (p/order-create! bb-order order-spot-market-buy))

