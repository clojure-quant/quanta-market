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
   [quanta.market.protocol]
   [demo.accounts :refer [accounts]]))
  
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

 
(def db (trade-db-start "/tmp/trade-db"))
 
(def tm (trade-manager-start db accounts))

 (comment 
   tm
 
   (get-account-ids tm)
   (get-account tm :florian/test1)
 
  (start-all-accounts tm)
  
  (stop-all-accounts tm)
   
 ;  
   )
 
 
 
 


 
 
 
 
 
 