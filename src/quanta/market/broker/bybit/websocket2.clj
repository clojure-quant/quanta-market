(ns quanta.market.broker.bybit.websocket2
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [manifold.deferred :as d]
   [manifold.stream :as ms]
   [quanta.market.util :refer [flow-sender cont]]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.connection :as c]
   [quanta.market.broker.bybit.task.auth :as a]
   [quanta.market.broker.bybit.pinger :as pinger])
  (:import [missionary Cancelled]))

(defn connect-impl! [flow-sender-in flow-sender-out opts ping label]
  (m/sp
   (try
     (info label "connecting to bybit websocket opts: " opts)
     (let [c (m/? (c/connection-start! flow-sender-in flow-sender-out opts label))]
       (info label "bybit websocket2 got a new connection: " c)
       (when-let [creds (:creds opts)]
         (info label "websocket auth " opts)
         (m/? (a/authenticate! c creds)))
      ; pinger
     ;(pinger/stop-pinger ping) ; on reconnect, old pinger needs to be stopped
     ;(pinger/start-pinger c ping)
       (info "returning connection: " c)


       c)
     (catch Cancelled _
       (println "connectimpl shutting down..")
       nil))))

(def delays                          ;; Our backoff strategy :
  (->> 1000                          ;; first retry is delayed by 1 second
       (iterate (partial * 2))       ;; exponentially grow delay
       (take 5)))                    ;; give up after 5 retries

(defn backoff [request delays]
  (if-some [[delay & delays] (seq delays)]
    (m/sp
     (try (m/? request)
          (catch Exception e
            (if (-> e ex-data :worth-retrying)
              (do (m/? (m/sleep delay))
                  (m/? (backoff request delays)))
              (throw e)))))
    request))

(defn connect!
  "tries to connect up to 5 times with exponential backoff strategy.
   returns a missionary task"
  [flow-sender-in flow-sender-out opts ping label]
  (let [connect-t (connect-impl! flow-sender-in flow-sender-out opts ping label)]
    (backoff connect-t delays)))

(defn disconnect! [conn ping label]
  (try
    ;(pinger/stop-pinger ping)
    ;(ms/close! (:stream conn))
    (catch Exception ex
      (info label "disconnect exception: " ex))))


(defn create-conn-f2 [opts flow-sender-in flow-sender-out ping label]
  (m/signal ; signal is continuous, and therefore allows reuse of existing connection
   (cont 
    (m/ap
     (m/amb nil)
     (loop [conn (m/? (connect-impl! flow-sender-in flow-sender-out opts ping label))]
       (info "conn-f got a connection: " conn)
       (let [consumer-t (:consumer-task conn)]
         (m/amb
          conn
          (let [reconnect? (try (m/? (m/sleep 50000)) ; delays are built into the connect task
                               ;(m/? consumer-t)
                                
                                true
                                (catch Cancelled _
                                  (do (info label "websocket got cancelled.")
                                      (disconnect! conn ping label)
                                      false)))]
            (if reconnect?
              (recur (m/? (connect-impl! flow-sender-in flow-sender-out opts ping label)))
              (reduced nil))))))))))

#_(defn create-conn-f [opts flow-sender-in flow-sender-out ping label]
    (m/signal ; signal is continuous, and therefore allows reuse of existing connection
     (m/ap
      (m/amb nil)
      nil
      (loop []
        (let [conn (m/? (connect-impl! flow-sender-in flow-sender-out opts ping label))]
          (info "conn-f got a connection: " conn)
          (m/amb conn)
          (m/? (m/sleep 120000))
          (recur))))))

(defn reconnect! [flow-sender-in flow-sender-out opts ping label consumer-t]
  (m/sp (m/? consumer-t)
        (println "RECONNECTING!!")
        (m/? (connect-impl! flow-sender-in flow-sender-out opts ping label))))

(defn create-conn-f [opts flow-sender-in flow-sender-out ping label]
  (m/signal
   (cont 
   (m/ap
    (let [conn-a (atom nil)]
      (try
        (loop [conn (m/? (connect-impl! flow-sender-in flow-sender-out opts ping label))]
          (let [consumer-t (:consumer-task conn)]
            (reset! conn-a conn)
            (m/amb conn (recur (m/? (reconnect! flow-sender-in flow-sender-out opts ping label consumer-t))))))
        (catch Cancelled _
          (println "shutting down..")
          ;(m/?  (disconnect! @conn-a ping label))
          true)))))))

(defrecord bybit-websocket2 [conn-f flow-sender-in flow-sender-out label]
  p/connection
  (connection-flow [this]
    conn-f)
  (msg-in-flow [this]
    ;(info "returning :flow flow-sender-in:  " flow-sender-in)
    (:flow flow-sender-in))
  (msg-out-flow [this]
    (:flow flow-sender-out))
  ; bybit websocket
  )


(defn create-websocket2
  [opts label]
  (info "wiring up bybit-websocket : " opts)
  (let [flow-sender-in (flow-sender)
        flow-sender-out (flow-sender)
        ping (atom nil)
        conn-f (create-conn-f opts flow-sender-in flow-sender-out ping label)]
    (bybit-websocket2. conn-f
                       flow-sender-in
                       flow-sender-out
                       label)))




