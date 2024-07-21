(ns quanta.market.connection
  (:require
   [missionary.core :as m]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.trade.db :as trade-db :refer [trade-db-start
                                                trade-db-stop]]
   [quanta.market.trade.msg-logger :refer [start-logger! stop-logger!]]
   [quanta.market.util :refer [mix]]
    ; default implementations:
   ;[quanta.market.broker.paper]
   [quanta.market.broker.bybit]))

(defn get-account [{:keys [accounts] :as _this} account-id]
  (get accounts account-id))

(defn get-account-ids [{:keys [accounts] :as _this}]
  (keys accounts))



(defrecord connection-manager [accounts db msg-logger-in msg-logger-out]
  ;
  p/connection
  (start! [this {:keys [account]}]
    (info "starting account: " account)
    (if-let [a (get-account this account)]
      (let [{:keys [opts]} a
            _ (info "account: " account " opts: " opts)
            conn (p/start! a opts)]
        (start-logger! db msg-logger-in account :in (p/msg-in-flow a))
        (start-logger! db msg-logger-out account :out (p/msg-out-flow a))
        conn)))
  (stop! [this {:keys [account]}]
    (if-let [a (get-account this account)]
      (let [{:keys [opts]} a]
        (stop-logger! msg-logger-in account)
        (stop-logger! msg-logger-out account)
        (p/stop! a opts))))
  (msg-in-flow [this]
    (let [account-msg-in-flows (map p/msg-in-flow (vals (:accounts this)))]
      (apply mix account-msg-in-flows)))
  (msg-out-flow [this]
    (let [account-msg-out-flows (map p/msg-out-flow (vals (:accounts this)))]
      (apply mix account-msg-out-flows)))
   ;
  )

(defn create-account [[id opts]]
  [id (p/create-account (assoc opts :account-id id))])

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

(defn print-messages [{:keys [db] :as this} opts]
  (trade-db/print-messages db opts))


(defn connection-manager-start [db accounts]
  (let [accounts (create-accounts accounts)
        msg-logger-in (atom {})
        msg-logger-out (atom {})]
    (connection-manager. accounts db msg-logger-in msg-logger-out)))

(defn connection-manager-stop [{:keys [db accounts] :as this}]
  (stop-all-accounts this)
  ;(trade-db-stop db)
  )



