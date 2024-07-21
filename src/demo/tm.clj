(ns demo.tm
  (:require
   [modular.log]
   [quanta.market.trade.db :as trade-db :refer [trade-db-start
                                                trade-db-stop]]
   [quanta.market.trade :refer [trade-manager-start
                                start-all-accounts
                                stop-all-accounts
                                get-account-ids
                                get-account]]
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
  (let [tm (trade-manager-start db accounts)]
    (start-logging ".data/msg-in.txt" (p/msg-in-flow tm))
    (start-logging ".data/order-update-msg.txt"
                   (p/order-update-msg-flow  tm))
    (start-logging ".data/order-update.txt"
                   (p/order-update-flow  tm))
    (start-all-accounts tm)
    tm))


(def tm (setup-connection-manager))

(comment
  tm

  (get-account-ids tm)
  (get-account tm :florian/test1)

  (start-all-accounts tm)
  (stop-all-accounts tm)

 ;  
  )
 
 
 
 


 
 
 
 
 
 