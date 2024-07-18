(ns quanta.market.trade
  (:require
   [quanta.market.protocol :as p]
   [quanta.market.trade.db :refer [trade-db-start trade-db-stop]]
    ; default implementations:
   [quanta.market.broker.random]
   [quanta.market.broker.bybit]))


(defrecord trade-manager [accounts db]
  p/trade
  (order-create! [{:keys [accounts] :as _this} {:keys [account] :as order}]
    (if-let [a (get accounts account)]
      (p/order-create! a order)))
  (order-cancel! [{:keys [accounts] :as _this} {:keys [account] :as order-cancel}]
    (if-let [a (get accounts account)]
      (p/order-cancel! a order-cancel))))

(defn create-account [[id opts]]
  [id (p/create-account opts)])

(defn create-accounts [accounts]
  (->> accounts
       (map create-account)
       (into {})))

(defn start-all-accounts [{:keys [db accounts] :as this}]
  (let [account-vals (vals accounts)]
    (doall (map #(p/start! %) account-vals))))

(defn stop-all-accounts [{:keys [db accounts] :as this}]
  (let [account-vals (vals accounts)]
    (doall (map #(p/stop! %) account-vals))))


(defn trade-manager-start [db-path accounts]
  (let [accounts (create-accounts accounts)
        db (trade-db-start db-path)]
    (trade-manager. accounts db)))

(defn trade-manager-stop [{:keys [db accounts] :as this}]
  (stop-all-accounts this)
  (trade-db-stop db))_




