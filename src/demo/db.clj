(ns demo.db
   (:require
    [missionary.core :as m]
    [quanta.market.trade.msg-logger :refer [create-logger!]]
    [quanta.market.trade :refer [query-messages print-messages]]
    [clojure.pprint :refer [print-table]]
    [demo.tm :refer [tm]]))
  

; test storing messages from a flow

(def conn {:msg-flow (m/seed [1 2 3])})

(def db (:db tm))


(def dispose! 
  (create-logger! db :florian/test1 conn))



dispose!

(dispose!)


(defn print-history [messages]
  (print-table [:message/timestamp
                :message/direction
                :message/data
                ] messages))


 (print-messages tm {:account :florian/test1})

 (print-messages tm {:account :rene/test1})

 (print-messages tm {:account :rene/test1-orderupdate})

 (print-messages tm {:account :bybit})





