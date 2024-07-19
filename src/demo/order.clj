(ns demo.order
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [demo.tm :refer [tm]]))

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
   :qty "0.0001"
   ;:ordertype :market
   :ordertype :limit
   :limit "68750.0"
   })


(def order-linear
  (assoc order-spot :asset "BTC-27SEP24.L"))

(def order-inverse
  (assoc order-spot :asset "BTCUSD.I"))

{:code 170121,
 :order {:account :rene/test2, :asset "ETHUSDT.S", :side :buy, :qty "0.01", :limit "1000.0"},
 :msg/type :order/rejected,
 :message "Invalid symbol."}

{:code 170131,
 :order {:account :rene/test2, :asset "BTCUSDT.S", :side :buy, :qty "0.01", :limit "1000.0"},
 :msg/type :order/rejected,
 :message "Insufficient balance."}


(m/?  (p/order-create! tm order-spot))

(m/?  (p/order-create! tm order-linear))
(m/?  (p/order-create! tm order-inverse))
;; => {:order {:account :rene/test1, :asset "ETHUSDT", :side :buy, :qty "0.01", :limit "1000.0"},
;;     :message "Permission denied for current apikey",
;;     :code 10005,
;;     :msg/type :order/rejected}

{:order {:account :rene/test4, :asset "BTCUSDT.S", :side :buy, :qty "0.0001", :type :market, :limit "1000.0"},
 :msg/type :order/confirmed}

(def cancel
  {:account :rene/test1
   :asset "ETHUSDT"
   :order-id "my-id-007"})


(m/? (p/order-cancel! tm cancel))




