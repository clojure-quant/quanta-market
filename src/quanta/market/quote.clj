(ns quanta.market.quote
  (:require
   [quanta.market.protocol :as p]
   [quanta.market.quote.current :as current]
  ; bring default implementations into scope:
   [quanta.market.broker.bybit.quotefeed]))

(defn get-feed [{:keys [quotefeeds] :as _this} account-id]
  (get quotefeeds account-id))

(defn get-account-ids [{:keys [quotefeeds] :as _this}]
  (keys quotefeeds))

(defrecord quote-manager [quotefeeds]
  p/quotefeed
  (start-quote [this]
    (doall (map p/start-quote (vals quotefeeds)))
    (keys quotefeeds))
  (stop-quote [this]
    (doall (map p/stop-quote (vals quotefeeds)))
    (keys quotefeeds))
  (subscribe-last-trade! [this {:keys [account] :as sub}]
    (when-let [feed (get-feed this account)]
      (p/subscribe-last-trade! feed sub)))
  (unsubscribe-last-trade! [this {:keys [account] :as sub}]
    (when-let [feed (get-feed this account)]
      (p/unsubscribe-last-trade! feed sub)))
  (last-trade-flow [this {:keys [account] :as account-asset}]
    (when-let [feed (get-feed this account)]
      (p/last-trade-flow feed account-asset)))
  p/quote
  (get-quote [this sub]
    (current/get-quote this sub))
    ;
  )

(defn create-account [[id opts]]
  [id (p/create-quotefeed (assoc opts :account-id id))])

(defn create-accounts [accounts]
  (->> accounts
       (map create-account)
       (into {})))


(defn quote-manager-start [accounts]
  (let [quotefeeds (create-accounts accounts)]
    (quote-manager. quotefeeds)))

(defn quote-manager-stop [{:keys [db accounts] :as this}]
  ;
  )