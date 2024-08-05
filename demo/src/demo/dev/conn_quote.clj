(ns demo.dev.conn-quote
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop! flow-sender current-v]]
   [quanta.market.broker.bybit.connection :as c]
   [quanta.market.broker.bybit.websocket2 :refer [create-websocket2 create-conn-f ]]
   [quanta.market.broker.bybit.task.subscription :refer [subscription-start-msg]]
   [quanta.market.broker.bybit.rpc :refer [rpc-req!]]
      [quanta.market.broker.bybit.topic :refer [format-topic-sub topic-data-flow topic-transformed-flow]]

   ))


;; test for the establishment of websocket stream
(def conn
  (m/? (c/connect! {:mode :main
                    :segment :spot
                    :label :test123})))

conn
;; test for connection-start!

(m/? 
(c/connection-start!
 (flow-sender)
 (flow-sender)
 {:mode :main
  :segment :spot}
 :test123) 
 )



current-v

(m/? (current-v 
(m/stream (m/seed [nil nil nil 1 2 3]))        
      ))




;; test for create-conn-f

(def conn-f 
(create-conn-f {:mode :main
                :segment :spot}
               (flow-sender)
               (flow-sender)
               (atom nil)
               :demo5))








(m/? (current-v conn-f))





;; => [{}, true, true, true, #error {
;;     :cause "Dataflow variable derefence cancelled."
;;     :via
;;     [{:type missionary.Cancelled
;;       :message "Dataflow variable derefence cancelled."}]
;;     :trace
;;     []}]


  
 

conn

(def ws (create-websocket2 {:mode :main
                            :segment :spot}
                           :test123))

(start-flow-logger!
 ".data/test-msg-in.txt"
 :msg-in
 (p/msg-in-flow ws))


(start-flow-logger!
 ".data/test-msg-out.txt"
 :msg-out
 (p/msg-out-flow ws))

(def topic (format-topic-sub {:asset "ETHUSDT"
                              :topic :asset/trade}))

topic

(def msg (subscription-start-msg topic))

msg

(m/? (rpc-req! ws msg identity))


(stop! :msg-in)
(stop! :msg-out)
