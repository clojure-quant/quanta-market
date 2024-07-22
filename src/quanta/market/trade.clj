(ns quanta.market.trade
  (:require
   [clojure.string :as str]
   [missionary.core :as m]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [mix start-logging]]
   ; bring default implementations into scope:
   [quanta.market.broker.bybit.tradeaccount]))

(defn get-tradeaccount [{:keys [tradeaccounts] :as _this} account-id]
  (get tradeaccounts account-id))

(defrecord trade-manager [tradeaccounts log-dir]
  p/tradeaccount
  (start-trade [this]
    (doall (map p/start-trade (vals tradeaccounts)))
    (keys tradeaccounts))
  (stop-trade [this]
    (doall (map p/stop-trade (vals tradeaccounts)))
    (keys tradeaccounts))
  (order-create! [this {:keys [account] :as order}]
    (if-let [ta (get-tradeaccount this account)]
      (p/order-create! ta order)))
  (order-cancel! [this {:keys [account] :as order-cancel}]
    (if-let [ta (get-tradeaccount this account)]
      (p/order-cancel! ta order-cancel)))
  (order-update-msg-flow [this]
    (let [account-flows (map p/order-update-msg-flow (vals tradeaccounts))]
      (apply mix account-flows)))
  (order-update-flow [this]
    (let [account-flows (map p/order-update-flow (vals tradeaccounts))]
      (apply mix account-flows)))
   (msg-flow [this]
    (let [account-flows (map p/msg-flow (vals tradeaccounts))]
      (apply mix account-flows))))

(defn account-log-filename [log-dir id]
  (let [account-str  (-> (str "account-" id ".txt")
                         (str/replace #":" "")
                         (str/replace #"/" "_"))]
    (str log-dir account-str)))

(defn create-account [log-dir [id opts]]
  (let [account (p/create-tradeaccount (assoc opts :account-id id))
        log-filename (account-log-filename log-dir id) ]
    (when log-dir
      (info "logging " id " to file: " log-filename)
      (start-logging log-filename (p/msg-flow account)))
    [id account]))

(defn create-accounts [accounts log-dir]
  (->> accounts
       (map #(create-account log-dir %))
       (into {})))

(defn trade-manager-start [accounts log-dir]
  (let [tradefeeds (create-accounts accounts log-dir)]
    (trade-manager. tradefeeds log-dir)))


(defn trade-manager-stop [this]
  (p/stop-trade this)
  ;
  )

(comment 
  (account-log-filename "./data/" :rene/test1-bybit)
  
 ; 
  )