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



(def qsub {:account :bybit
           :asset "BTCUSDT"})


 ; log all messages (for testing)
(start-flow-logger!
 ".data/quotes9.txt"
 :quote/msg
 (p/get-quote bb-quote qsub))


(stop! :quote/msg)



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






