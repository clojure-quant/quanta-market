(ns demo.tm
  (:require 
   [missionary.core :as m]
   [quanta.market.trade :refer [trade-manager-start start-all-accounts get-account-ids get-account]]
   [quanta.market.protocol]
   [demo.accounts :refer [accounts]]))
 
 (def tm (trade-manager-start "/tmp/trade-db" accounts))

 tm

(get-account-ids tm)
 (get-account tm :florian/test1)


(start-all-accounts tm)
 
 