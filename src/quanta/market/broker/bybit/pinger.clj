(ns quanta.market.broker.bybit.pinger
  (:require
    [taoensso.timbre :as timbre :refer [debug info warn error]]
    [missionary.core :as m]
    [quanta.market.broker.bybit.task.ping :refer [ping!]])
   (:import [missionary Cancelled]))
  
(defn ping-task [conn]
  (m/sp
   (info "ping task startd..")
   (try
     (loop [i 0]
       (m/? (m/sleep 1000))
       (m/? (ping! conn))
       (recur (inc i)))  
       (catch Cancelled _ true)
       (catch Exception _ false))))

(defn start-pinger [conn ping]
  (if conn 
    (let [_ (info "creating ping task..")
          pinger (ping-task conn)
          ping-stop (pinger #(info "pinger stopped successfully: " %)
                            #(info "pinger crashed: " %))]
       (reset! ping ping-stop))
    (do 
      (error "cannot start pinger - no connection.")
      (reset! ping nil))))


(defn stop-pinger [ping]
  (let [ping-dispose @ping]
    (when ping-dispose
      (info "stopping pinger..")
      (ping-dispose)
      (reset! ping nil))))

