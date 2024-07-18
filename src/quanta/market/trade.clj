(ns quanta.market.trade
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.trade.db :refer [trade-db-start trade-db-stop]]
    ; default implementations:
   ;[quanta.market.broker.paper]
   [quanta.market.broker.bybit]))


(defn get-account [{:keys [accounts] :as _this} account-id]
  (get accounts account-id))

(defn get-account-ids [{:keys [accounts] :as _this}]
  (keys accounts))

(defrecord trade-manager [accounts db]
  ;
  p/connection
  (start! [this {:keys [account]}]
    (info "starting account: " account)
    (if-let [a (get-account this account)]
      (let [{:keys [opts]} a]
        (info "account: " account " opts: " opts)
        (p/start! a opts))))
  (stop! [this {:keys [account]}]
    (if-let [a (get-account this account)]
      (let [{:keys [opts]} a]
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
    (doall (map #(p/start! this {:account %}) account-ids))))

(defn stop-all-accounts [{:keys [db accounts] :as this}]
  (let [account-ids (get-account-ids this)]
    (doall (map #(p/stop! this {:account %}) account-ids))))


(defn trade-manager-start [db-path accounts]
  (let [accounts (create-accounts accounts)
        db (trade-db-start db-path)]
    (trade-manager. accounts db)))

(defn trade-manager-stop [{:keys [db accounts] :as this}]
  (stop-all-accounts this)
  (trade-db-stop db))




