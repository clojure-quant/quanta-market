(ns demo.tm
  (:require
   [quanta.market.protocol :as p]
   [quanta.market.trade.db :as trade-db :refer [trade-db-start
                                                trade-db-stop]]
   [quanta.market.trade :refer [trade-manager-start]]
   [quanta.market.util :refer [start-logging start-printing]]
   [quanta.market.portfolio :refer [portfolio-manager-start
                                    get-working-orders]]
   [demo.accounts :refer [accounts-trade]]
    [demo.logging] ; for side effects
   ))



(def db (trade-db-start ".data/trade-db"))

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

(def pm (portfolio-manager-start
         {:tm tm
          :db nil
          :transactor-logfile ".data/transactor2.txt"
          :alert-logfile ".data/transactor-error2.txt"}))





(comment
  tm

  (p/start-trade tm)
  (p/stop-trade tm)
  
  (start-printing (p/msg-flow tm) "trade msg:")
  
  
  (get-working-orders pm)


 ;  
  )
 
 
 
 


 
 
 
 
 
 