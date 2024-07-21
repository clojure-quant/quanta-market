(ns demo.order
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [demo.pm :refer [pm]]
   [quanta.market.portfolio :refer [create-order get-working-orders]]
   ))

(def assets
  [; spot
   "BTCUSDT.S" "BTCUSDC.S"
   ; linear
   "BTC-27JUN25.L"
   "BTC-26JUL24.L"
   "BTC-02AUG24.L"
   "BTC-09AUG24.L"
   "BTC-30AUG24.L"
   "BTC-27SEP24.L"
   "BTC-27DEC24.L"
   "BTC-28MAR25.L"
   "BTCPERP.L"
   "BTCUSDT.L"
   ; inverse
   "BTCUSD.I"
   "BTCUSDU24.I"
   "BTCUSDZ24.I"])


(def order-spot
  {:account :rene/test4
   :asset "BTCUSDT.S"
   :side :buy
   :qty 0.0001
   :ordertype :limit
   :limit 68750.0
   })

(m/? (create-order pm order-spot))



(def cancel
  {:account :rene/test1
   :asset "ETHUSDT"
   :order-id "my-id-007"})


(m/? (p/order-cancel! pm cancel))



(get-working-orders pm)


(comment 
  
(format "%f" 3.4)
(str (:qty order-spot))
(format "%f" (:qty order-spot))
(format "%f" (:limit  order-spot))  

  (def order-linear
    (assoc order-spot :asset "BTC-27SEP24.L"))
  
  (def order-inverse
    (assoc order-spot :asset "BTCUSD.I"))
  
  
; 
  )







