(ns demo.dev.conn
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-logging start-printing]]
   [demo.accounts :refer [accounts-trade]]))

(def account (get accounts-trade :rene/test4))

account

(def ta (p/create-tradeaccount account))

(p/start-trade ta)

(p/stop-trade ta)

(start-logging ".data/test-account.txt"
               (p/msg-flow ta))

(start-printing (p/msg-flow ta)
                "test-msg: "
                )

(def order-spot
  {:account :rene/test4
   :asset "BTCUSDT.S"
   :side :buy
   :qty 0.0001
   :ordertype :limit
   :limit 68750.0})


(m/? (p/order-create! ta order-spot))