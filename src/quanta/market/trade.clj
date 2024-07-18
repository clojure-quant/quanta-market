(ns quanta.market.trade
  (:require
   [missionary.core :as m]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.trade.db :as trade-db :refer [trade-db-start 
                                                trade-db-stop]]
   [quanta.market.trade.msg-logger :refer [start-logger! stop-logger!]]
    ; default implementations:
   ;[quanta.market.broker.paper]
   [quanta.market.broker.bybit]))


(defn get-account [{:keys [accounts] :as _this} account-id]
  (get accounts account-id))

(defn get-account-ids [{:keys [accounts] :as _this}]
  (keys accounts))



(defrecord trade-manager [accounts db msg-logger]
  ;
  p/connection
  (start! [this {:keys [account]}]
    (info "starting account: " account)
    (if-let [a (get-account this account)]
      (let [{:keys [opts]} a
            _ (info "account: " account " opts: " opts)
            conn (p/start! a opts)]
       (start-logger! db msg-logger account conn)
        conn
        )))
  (stop! [this {:keys [account]}]
    (if-let [a (get-account this account)]
      (let [{:keys [opts]} a]
        (stop-logger! msg-logger account) 
        (p/stop! a opts))))
  ;
  p/trade
  (order-create! [this {:keys [account] :as order}]
    (if-let [a (get-account this account)]
      (p/order-create! a order)))
  (order-cancel! [this {:keys [account] :as order-cancel}]
    (if-let [a (get-account this account)]
      (p/order-cancel! a order-cancel))))

(defn create-account [[id opts]]
  [id (p/create-account opts)])

(defn create-accounts [accounts]
  (->> accounts
       (map create-account)
       (into {})))

(defn start-all-accounts [{:keys [db accounts] :as this}]
  (let [account-ids (get-account-ids this)]
    (doall (map #(p/start! this {:account %}) account-ids))
    account-ids))

(defn stop-all-accounts [{:keys [db accounts] :as this}]
  (let [account-ids (get-account-ids this)]
    (doall (map #(p/stop! this {:account %}) account-ids))
    account-ids))


(defn query-messages [{:keys [db] :as this} opts]
  (trade-db/query-messages db opts))

(defn trade-manager-start [db-path accounts]
  (let [accounts (create-accounts accounts)
        db (trade-db-start db-path)
        msg-logger (atom {})]
    (trade-manager. accounts db msg-logger)))

(defn trade-manager-stop [{:keys [db accounts] :as this}]
  (stop-all-accounts this)
  (trade-db-stop db))




