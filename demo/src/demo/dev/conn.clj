(ns demo.dev.conn
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-logging start-printing]]
   [quanta.market.broker.bybit.task.order :refer [order-create-raw! order->bybit-format]]
   [demo.accounts :refer [accounts-trade]]
    ; bring default implementations into scope:
   [quanta.market.broker.bybit.tradeaccount]))

(def account (get accounts-trade :rene/test4))

account

(def ta (p/create-tradeaccount account))

(p/start-trade ta)

(p/stop-trade ta)

(start-logging ".data/test-account-msg6.txt"
               (p/msg-flow ta))

(start-logging ".data/test-account-orderupdate6.txt"
               (p/order-update-flow ta))

(start-printing (p/msg-flow ta)
                "test-msg: ")

(def order-spot-limit
  {:account :rene/test4
   :asset "BTCUSDT.S"
   :side :buy
   :qty 0.001
   :ordertype :limit
   :limit 68750.0})

(def order-spot-market
  {:account :rene/test4
   :asset "BTCUSDT.S"
   :side :sell
   :qty 0.001
   :ordertype :market})

(order->bybit-format order-spot-market)
;; => {"symbol" "BTCUSDT",
;;       "category" "spot", 
;;       "side" "Buy", 
;;       "orderType" "Market", 
;;       "qty" "0.001"}

(order->bybit-format order-spot-limit)
;; => {"price" "68750.000", 
;;       "symbol" "BTCUSDT", 
;;       "category" "spot", 
;;       "side" "Buy", 
;;        "orderType" "Limit", 
;;         "qty" "0.001"}

(m/? (p/order-create! ta order-spot-market))

(m/? (p/order-create! ta order-spot-limit))

(m/? (p/order-cancel! ta {:order-id "234"}))
(m/? (order-create-raw!
      (p/connection-flow (:websocket-order ta))
      {"price" "68750.00",
       "symbol" "BTCUSDT",
       "side" "Buy",
       "orderType" "Limit",
       "qty" "0.0001",
       "category" "spot",
       "timeInForce" "PostOnly"}))
;; => {:retCode 170136,
;;     :retMsg "Order quantity exceeded lower limit.",
;;     :connId "cpv85t788smd5eps8ncg-30va",
;;     :op "order.create",
;;     :header
;;     {:Timenow "1721676768573",
;;      :X-Bapi-Limit-Status "19",
;;      :X-Bapi-Limit-Reset-Timestamp "1721676768572",
;;      :Traceid "2ae9bb47095d439d523decac4b91eaa4",
;;      :X-Bapi-Limit "20"},
;;     :reqId "6ZlVcsHu",
;;     :data {}}

;; => {:retCode 0,
;;     :retMsg "OK",
;;     :connId "cpv85t788smd5eps8ncg-30va",
;;     :op "order.create",
;;     :header
;;     {:Timenow "1721676743313",
;;      :X-Bapi-Limit-Status "19",
;;      :X-Bapi-Limit-Reset-Timestamp "1721676743312",
;;      :Traceid "1bdf1e6f48a2640a0af7a40e57a037f1",
;;      :X-Bapi-Limit "20"},
;;     :reqId "1K7hFHG9",
;;     :data {:orderLinkId "1735944774881732609", :orderId "1735944774881732608"}}

order-create-raw!

{:message nil,
 :msg
 {"op" "order.create",
  "header" {"X-BAPI-TIMESTAMP" 1721673555066, "X-BAPI-RECV-WINDOW" "8000", "Referer" "bot-001"},
  "args"
  [{"price" "68750.000000",
    "symbol" "BTCUSDT",
    "side" "Buy",
    "orderType" "Limit",
    "qty" "0.000100",
    "category" "spot",
    "timeInForce" "PostOnly"}]},
 :msg/type :order/rejected,
 :order {:account :rene/test4, :asset "BTCUSDT.S", :side :buy, :qty 1.0E-4, :ordertype :limit, :limit 68750.0},
 :code nil}
