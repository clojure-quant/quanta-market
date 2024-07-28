(ns demo.dev.quote2
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop! start-flow-printer!]]
   [demo.logging] ; for side effects
   [quanta.market.broker.bybit.quotefeed2]
   [quanta.market.broker.bybit.websocket2 :refer [create-websocket2]]
   ))

(def bb-quote (p/create-quotefeed {:type :bybit2}))


 ; log all messages (for testing)
(start-flow-logger!
 ".data/bybit-trade4.txt"
 :trade/msg
 (p/get-topic bb-quote {:topic :asset/trade
                        :asset "BTCUSDT"}))

(start-flow-logger!
 ".data/bybit-stats3.txt"
 :stats/msg
 (p/get-topic bb-quote {:topic :asset/stats
                        :asset "BTCUSDT"}))

(start-flow-logger!
 ".data/bybit-liquidation.txt"
 :liquidation/msg
 (p/get-topic bb-quote {:topic :asset/liquidation
                        :asset "BTCUSDT"}))

(start-flow-logger!
 ".data/bybit-bars-unfinished.txt"
 :bars-unfinished/msg
 (p/get-topic bb-quote {:topic :asset/bars
                        :interval "1"
                        :asset "BTCUSDT"}))

(start-flow-logger!
 ".data/bybit-bars-finished.txt"
 :bars-finished/msg
 (p/get-topic bb-quote {:topic :asset/bars
                        :interval "1"
                        :asset "BTCUSDT"
                        :only-finished? true}))



(start-flow-logger!
 ".data/bybit-orderbook.txt"
 :orderbook/msg
 (p/get-topic bb-quote {:topic :asset/orderbook
                        :depth 1
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
                      :segment :spot})
  )

(def conn-f (p/current-connection ws2))

(start-flow-printer!
 conn-f
 :quote/conn)

(stop! :quote/conn)


(start-flow-printer!
  (m/seed [1 2 3 4 5])
 :test
 )






