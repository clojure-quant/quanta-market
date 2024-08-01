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

(defn connect! [flow-sender-in flow-sender-out opts ping]
  (info "connecting to bybit websocket opts: " opts)
  (let [c (c/connection-start! flow-sender-in flow-sender-out opts)]
    (debug "bybit websocket2 got a new connection: " c)
    (when-let [creds (:creds opts)]
      (info "websocket auth " opts)
      (m/? (a/authenticate! c creds)))
      ; pinger
      (pinger/stop-pinger ping) ; on reconnect, old pinger needs to be stopped
      (pinger/start-pinger c ping)
    c))

(defn disconnect! [conn ping]
  (try 
    (pinger/stop-pinger ping)
    (ms/close! (:stream conn))
    (catch Exception ex
      (info "disconnect exception: " ex))))


(defn disconnected? [conn]
  (let [sc (:stream-consumer conn)]
    @sc))

(defn disconnected-t [conn]
  (m/sp (let [sc (:stream-consumer conn)]
          @sc)))

(defn await-deferred
  "Returns a task completing with the result of given deferred"
  [df]
  (let [v (m/dfv)]
    (d/on-realized df
                   (fn [x]
                     (info "deferred success: " x)
                     (v x))
                   (fn [x]
                     (info "deferred error: " x)
                     (v (fn [] (throw x)))))
    (m/absolve v)))

(defn create-conn-f [opts flow-sender-in flow-sender-out ping]
  (m/signal ; signal is continuous, and therefore allows reuse of existing connection
   (m/ap
    (loop [conn (connect! flow-sender-in flow-sender-out opts ping)]
      (let [sc (:stream-consumer conn)
            conn-t (await-deferred sc)]
        (m/amb
         conn
         (let [reconnect? (try (m/? conn-t)
                               ;(m/? (m/via m/blk @sc))
                               (m/? (m/sleep 5000))
                               true
                               (catch Cancelled _
                                 (do (info "websocket got cancelled.")
                                     (disconnect! conn ping)
                                     false)))]
           (if reconnect?
             (recur (connect! flow-sender-in flow-sender-out opts ping))
             (reduced nil)
             ))))))))

(defrecord bybit-websocket2 [conn-f flow-sender-in flow-sender-out]
  p/connection
  (current-connection [this]
    conn-f)
  (msg-in-flow [this]
    ;(info "returning :flow flow-sender-in:  " flow-sender-in)
    (:flow flow-sender-in))
  (msg-out-flow [this]
    (:flow flow-sender-out))
  ; bybit websocket
  )


(defn create-websocket2
  [opts]
  (info "wiring up bybit-websocket : " opts)
  (let [flow-sender-in (flow-sender)
        flow-sender-out (flow-sender)
        ping (atom nil)
        conn-f (create-conn-f opts flow-sender-in flow-sender-out ping)]
    (bybit-websocket2. conn-f
                       flow-sender-in
                       flow-sender-out)))




