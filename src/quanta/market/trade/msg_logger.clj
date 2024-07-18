(ns quanta.market.trade.msg-logger
   (:require
    [missionary.core :as m]
    [taoensso.timbre :as timbre :refer [debug info warn error]]
    [quanta.market.trade.db :as trade-db :refer [store-message!]])
  (:import [missionary Cancelled])
  )

(defn log-messages-task [dbconn msg-flow account-id direction]
  (m/sp 
     (try 
       (info "msg-logger started for account: " account-id " direction: " direction)
       (m/? (m/reduce (fn [_r msg]
                               (debug "storing incoming msg: " msg)
                               (store-message! dbconn account-id direction msg)
                               msg)
                             nil
                             msg-flow))
       (catch Cancelled _ true))))

(defn create-logger! [dbconn account-id direction msg-flow]
  (assert dbconn)
  (assert msg-flow)
    (let [_ (info "creating msg-logger!" account-id direction)
          log-msg! (log-messages-task dbconn msg-flow account-id direction)
          dispose-logger (log-msg!
                          #(info "msg-logger stopped successfully " %)
                          #(error "msg-logger crashed!" %))]
      dispose-logger)
    (do 
      (error "cannot create msg logger - no msg-flow.")
      nil))

(defn start-logger! [dbconn msg-logger account-id direction conn]
  (assert msg-logger)
  (swap! msg-logger assoc account-id (create-logger! dbconn account-id direction conn)))

(defn stop-logger! [msg-logger account-id]
  (let [dispose-logger! (get @msg-logger account-id)]
   (when dispose-logger!
    (info "stopping msg logger for account " account-id)
    (dispose-logger!))
  (swap! msg-logger dissoc account-id)))






