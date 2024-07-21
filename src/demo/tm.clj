(ns demo.tm
  (:require
   [modular.log]
   [quanta.market.trade.db :as trade-db :refer [trade-db-start
                                                trade-db-stop]]
   [quanta.market.connection :refer [connection-manager-start
                                     start-all-accounts
                                     stop-all-accounts
                                     get-account-ids
                                     get-account]]
   [quanta.market.trade :refer [trade-manager-start]]
   [quanta.market.quote :refer [quote-manager-start]]
   [quanta.market.util :refer [start-logging]]
   [demo.accounts :refer [accounts]]
   [quanta.market.protocol :as p]))


(modular.log/timbre-config!
 {:min-level [[#{"org.apache.http.*"
                 "org.eclipse.aether.*"
                 "org.eclipse.jetty.*"
                 "modular.oauth2.*"
                 "modular.oauth2.token.refresh.*"
                 "modular.ws.*"
                 "webly.web.*"} :warn] ; webserver stuff - warn only
                                       ; [#{"modular.ws.*"} :debug]
              [#{"*"} :info]] ; default -> info
  :appenders {:default {:type :console-color}
              #_:rolling #_{:type :file-rolling
                            :path ".data/quanta.log"
                            :pattern :monthly}}})

(def db (trade-db-start ".data/trade-db"))

(defn setup-connection-manager []
  (let [cm (connection-manager-start db accounts)]
    (start-logging ".data/msg-in.txt" (p/msg-in-flow cm))
    (start-all-accounts cm)
    cm))

(defn setup-trade-manager [cm]
   (let [tm  (trade-manager-start cm)]
     (start-logging ".data/order-update-msg.txt"
                    (p/order-update-msg-flow  tm))
     (start-logging ".data/order-update.txt"
                    (p/order-update-flow  tm))
     tm))
  

(def cm (setup-connection-manager))

(def tm (setup-trade-manager cm))

(def qm (quote-manager-start cm))


(comment
  tm

  (get-account-ids cm)
  (get-account cm :florian/test1)

  (start-all-accounts cm)
  (stop-all-accounts cm)

 ;  
  )
 
 
 
 


 
 
 
 
 
 