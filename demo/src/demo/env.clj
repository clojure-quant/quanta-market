(ns demo.env
  (:require
   [quanta.market.quote :refer [quote-manager-start]]
   [demo.accounts :refer [accounts-quote]]
   [demo.logging] ; for side effects
   ))


(def qm (quote-manager-start accounts-quote))



