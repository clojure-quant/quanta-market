(ns demo.dev.quote2
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop! start-flow-printer!]]
   [quanta.market.broker.bybit.websocket2 :refer [create-websocket2]]
   [quanta.market.broker.bybit.quotefeed-category] ; for side effects
   [quanta.market.broker.bybit.quotefeed] ; side effects
   [demo.logging] ; for side effects
  ))

;; test the different kind of subscriptions that bybit supports

(def bb-quote (p/create-quotefeed {:type :bybit-category}))


 ; log all messages (for testing)
(start-flow-logger!
 ".data/bybit-trade4.txt"
 :trade/msg
 (p/get-topic bb-quote {:topic :asset/trade
                        :asset "ETHUSDT"}))

(start-flow-logger!
 ".data/bybit-trade6-perp.txt"
 :trade/msg
 (p/get-topic bb-quote {:topic :asset/trade
                        :asset "BTCUSDT.P"}))




(start-flow-logger!
 ".data/bybit-stats5.txt"
 :stats/msg
 (p/get-topic bb-quote {:topic :asset/stats
                        :asset "BTCUSDT"}))

(start-flow-logger!
 ".data/bybit-liquidation5.txt"
 :liquidation/msg
 (p/get-topic bb-quote {:topic :asset/liquidation
                        :asset "BTCUSDT"}))

(start-flow-logger!
 ".data/bybit-bars-unfinished5.txt"
 :bars-unfinished/msg
 (p/get-topic bb-quote {:topic :asset/bars
                        :interval "1"
                        :asset "BTCUSDT"}))

(start-flow-logger!
 ".data/bybit-bars-finished5.txt"
 :bars-finished/msg
 (p/get-topic bb-quote {:topic :asset/bars
                        :interval "1"
                        :asset "BTCUSDT"
                        :only-finished? true}))

(start-flow-logger!
 ".data/bybit-orderbook-5.txt"
 :orderbook/msg
 (p/get-topic bb-quote {:topic :asset/orderbook
                        :depth 1 ; spot: can be 1 50 200
                        :asset "BTCUSDT"}))

(stop! :trade/msg)
(stop! :stats/msg)
(stop! :liquidation/msg)
(stop! :bars-finished/msg)
(stop! :bars-unfinished/msg)
(stop! :orderbook/msg)


(start-flow-logger!
 ".data/quotes91.txt"
 :quote/msg2
 (p/get-quote bb-quote {:account :bybit
                        :asset "ETHUSDT"}))


(stop! :quote/msg2)


(def ws2 
   (create-websocket2 {:type :bybit2
                      :mode :main
                      :segment :spot}
                      [:bybit-test]
                      )
  )

(def conn-f (p/connection-flow ws2))

(start-flow-printer!
 conn-f
 :quote/conn)

(stop! :quote/conn)


(start-flow-printer!
  (m/seed [1 2 3 4 5])
 :test
 )






