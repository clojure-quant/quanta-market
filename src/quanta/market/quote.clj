(ns quanta.market.quote
  (:require
   [quanta.market.protocol :as p]
  ; bring default implementations into scope:
   [quanta.market.broker.bybit-quotefeed]
   ))

(defn get-feed [{:keys [quotefeeds] :as _this} account-id]
  (get quotefeeds account-id))

(defn get-account-ids [{:keys [quotefeeds] :as _this}]
  (keys quotefeeds))

(defrecord quote-manager [quotefeeds]
  p/quotefeed
  (socket [this]
    ; not applying to quote-manager really
    )
  (subscribe-last-trade! [this {:keys [account] :as sub}]
    (when-let [feed (get-feed this account)]
      (p/subscribe-last-trade! feed sub)))
  (unsubscribe-last-trade! [this {:keys [account] :as sub}]
    (when-let [feed (get-feed this account)]
      (p/unsubscribe-last-trade! feed sub)))
  (last-trade-flow [this {:keys [account] :as account-asset}]
    (when-let [feed (get-feed this account)]
      (p/last-trade-flow feed account-asset)))
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

(defn start-feed [this account-id]
  (let [feed (get-feed this account-id)
        socket (p/socket feed)
        opts (:opts feed)]
    (p/start! socket opts)))

(defn stop-feed [this account-id]
  (let [feed (get-feed this account-id)
        socket (p/socket feed)
        opts (:opts feed)]
    (p/stop! socket opts)))


(defn start-all-feeds [this]
  (let [account-ids (get-account-ids this)]
    (doall (map #(start-feed this %) account-ids))
    account-ids))

(defn stop-all-feeds [this]
  (let [account-ids (get-account-ids this)]
     (doall (map #(stop-feed this %) account-ids))
     account-ids))