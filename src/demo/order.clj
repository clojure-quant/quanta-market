(ns demo.order
  (:require 
    [missionary.core :as m]
    [quanta.market.protocol :as p]
    [demo.tm :refer [tm]]))

(def order
  {:account :florian/test1
   :asset "ETHUSDT"
   :side :buy
   :qty "0.01"
   :limit "1000.0"})

(m/?  (p/order-create! tm order))


(def cancel
  {:account :florian/test1
   :asset "ETHUSDT"
   :order-id "my-id-007"})


(m/? (p/order-cancel! tm cancel))



