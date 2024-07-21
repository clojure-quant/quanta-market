(ns quanta.market.quote
  (:require
   [quanta.market.protocol :as p]
   [quanta.market.connection :as c]
   [quanta.market.util :refer [mix]]))

(defrecord quote-manager [cm]
  p/quote
  (subscribe-last-trade! [{:keys [cm] :as this}
                          {:keys [account] :as sub}]
    (if-let [a (c/get-account cm account)]
      (p/subscribe-last-trade! a sub)))
  (unsubscribe-last-trade! [{:keys [cm] :as this}
                            {:keys [account] :as sub}]
    (if-let [a (c/get-account cm account)]
      (p/unsubscribe-last-trade! a sub)))

  (last-trade-flow [{:keys [cm] :as this}
                    {:keys [account] :as account-asset}]
    (if-let [a (c/get-account cm account)]
      (p/last-trade-flow a account-asset)))
    ;
  )


(defn quote-manager-start [cm]
  (quote-manager. cm))

(defn quote-manager-stop [{:keys [db accounts] :as this}]
  ;
  )
