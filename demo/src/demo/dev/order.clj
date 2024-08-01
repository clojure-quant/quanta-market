(ns demo.dev.order
  (:require 
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [nano-id.core :refer [nano-id]]
   [quanta.market.util :refer [start-flow-logger! stop!]]
   [quanta.market.broker.bybit.trade-update :refer [create-trade-update-feed trade-update-msg-flow]]
   [quanta.market.broker.bybit.trade-action :refer [create-trade-action]]
   [demo.accounts :refer [accounts-trade]]
   [demo.logging] ; side effect
   ))

accounts-trade

(def opts (get accounts-trade :rene/test4))

opts

(def bb-orderupdate (create-trade-update-feed opts))


(start-flow-logger!
 ".data/bybit-orderupdate-msg.txt"
 :orderupdate-msg
 (trade-update-msg-flow bb-orderupdate))



 ; log all messages (for testing)
(start-flow-logger!
 ".data/bybit-orderupdate2.txt"
 :orderupdate
 (p/get-topic bb-orderupdate {:topic :order/update}))


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
