(ns demo.tm
  (:require
   [modular.log]
   [quanta.market.protocol :as p]
   [quanta.market.trade.db :as trade-db :refer [trade-db-start
                                                trade-db-stop]]
   [quanta.market.trade :refer [trade-manager-start]]
   [quanta.market.util :refer [start-logging start-printing]]
   [quanta.market.portfolio :refer [portfolio-manager-start
                                    get-working-orders]]
   [demo.accounts :refer [accounts-trade]]))

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

(defn setup-trade-manager []
  (let [tm  (trade-manager-start accounts-trade ".data/")]
    (start-logging ".data/order-update-msg2.txt"
                   (p/order-update-msg-flow  tm))
    (start-logging ".data/order-update2.txt"
                   (p/order-update-flow  tm))
    (start-logging ".data/order-all-msgs2.txt"
                   (p/msg-flow  tm))

    tm))

(def tm (setup-trade-manager))


(def pm (portfolio-manager-start
         {:db nil
          :tm tm
          :alert-logfile ".data/alerts2.txt"}))

(comment
  tm

  (p/start-trade tm)

  (start-printing (p/msg-flow tm) "trade msg:")
  

  (p/stop-trade tm)
  (get-working-orders pm)


 ;  
  )
 
 
 
 


 
 
 
 
 
 