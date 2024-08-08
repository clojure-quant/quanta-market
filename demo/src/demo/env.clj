(ns demo.env
  (:require
   [quanta.market.quote :refer [quote-manager-start]]
   [quanta.market.trade :refer [trade-manager-start]]
   [quanta.market.trade.db :as trade-db :refer [trade-db-start
                                                trade-db-stop]]
   [quanta.market.portfolio :refer [portfolio-manager-start]]
   [demo.env.accounts :refer [accounts-quote accounts-trade]]
   [demo.env.logging] ; for side effects
   ))


(def qm (quote-manager-start accounts-quote))

(defn setup-trade-manager []
  (let [tm  (trade-manager-start accounts-trade ".data/")]
    ;(start-logging ".data/order-update-all-msg.txt"
    ;               (p/order-update-msg-flow  tm))
    ;(start-logging ".data/order-update-all.txt"
    ;               (p/order-update-flow  tm))
    ;(start-logging ".data/order-all-msgs.txt"
    ;               (p/msg-flow  tm))

    tm))


(def tm (setup-trade-manager))


;(def db (trade-db-start ".data/trade-db"))

(def pm (portfolio-manager-start
         {:tm tm
          :db nil
          :transactor-logfile ".data/transactor3.txt"
          :alert-logfile ".data/transactor-error3.txt"}))




