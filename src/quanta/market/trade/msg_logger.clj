(ns quanta.market.trade.msg-logger
   (:require
    [missionary.core :as m]
    [taoensso.timbre :as timbre :refer [debug info warn error]]
    [quanta.market.trade.db :as trade-db :refer [store-message!]])
  (:import [missionary Cancelled])
  )


(defn log-incoming-messages-task [dbconn msg-flow account-id]
  (m/sp 
     (try 
       (info "incoming-msg-logger started for account: " account-id)
       (m/? (m/reduce (fn [_r msg]
                               (info "storing incoming msg: " msg)
                               (store-message! dbconn account-id :in msg)
                               msg)
                             nil
                             msg-flow))
       (catch Cancelled _ true))))

(defn create-logger! [dbconn account-id conn]
  (assert dbconn)
  (assert conn)
  (if-let [msg-flow (:msg-flow conn)]
    (let [_ (info "creating incoming msg-logger!")
          log-msg! (log-incoming-messages-task dbconn msg-flow account-id)
          dispose-logger (log-msg!
                          #(info "incoming-msg-logger stopped successfully " %)
                          #(error "incoming-msg-logger crashed!" %))]
      dispose-logger)
    (do 
      (error "cannot create incoming msg logger - no msg-flow.")
      nil)))

(defn start-logger! [dbconn msg-logger account-id conn]
  (assert msg-logger)
  (swap! msg-logger assoc account-id (create-logger! dbconn account-id conn)))

(defn stop-logger! [msg-logger account-id]
  (let [dispose-logger! (get @msg-logger account-id)]
   (when dispose-logger!
    (info "stopping incoming msg logger for account " account-id)
    (dispose-logger!))
  (swap! msg-logger dissoc account-id)))






