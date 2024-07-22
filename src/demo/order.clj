(ns demo.order
  (:require
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [demo.tm :refer [tm pm]]
   [quanta.market.portfolio :refer [create-order get-working-orders order-cancel!]]
   ;[quanta.market.algo.price :refer [get-last-trade-price]]
   ;[quanta.trade.algo.order :refer [almost-market-order]]
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


(m/? (p/order-create! tm order-spot))

(m/? (create-order pm order-spot))


(m/? (create-order pm order-spot))



(m/? (get-last-trade-price qm :bybit "BTCUSDT"))

;; => {:asset "BTCUSDT", :price 68319.75, :size 9.99E-4, :time 1721609641809}

(m/? (almost-market-order qm {:asset "BTCUSDT"
                              :side :buy
                              :account :rene/test4}))
;; => {:account :rene/test4, :ordertype :limit, :asset "BTCUSDT.S", :qty 1.0E-4, :limit 68165.8659, :side :buy}



(let [order (m/? (almost-market-order qm {:asset "BTCUSDT"
                                          :side :buy
                                          :account :rene/test4
                                          }))]
  (m/? (create-order pm order))
  )
;; => {:msg/type :order/rejected,
;;     :code 170134,
;;     :msg
;;     {"op" "order.create",
;;      "header" {"X-BAPI-TIMESTAMP" 1721609864720, "X-BAPI-RECV-WINDOW" "8000", "Referer" "bot-001"},
;;      "args"
;;      [{"orderLinkId" "5NnCLOm_",
;;        "symbol" "BTCUSDT",
;;        "side" "Buy",
;;        "orderType" "Limit",
;;        "qty" "0.000100",
;;        "price" "68103.608220",
;;        "category" "spot",
;;        "timeInForce" "PostOnly"}]},
;;     :order
;;     {:account :rene/test4,
;;      :ordertype :limit,
;;      :asset "BTCUSDT.S",
;;      :qty 1.0E-4,
;;      :limit 68103.60822,
;;      :side :buy,
;;      :order-id "5NnCLOm_",
;;      :date-created #inst "2024-07-22T00:57:44.716-00:00"},
;;     :message "Order price has too many decimals."}



(def cancel
  {:account :rene/test4
   :asset "BTCUSDT"
   :order-id "OKzcAvMD"})


(m/? (order-cancel! pm cancel))



(get-working-orders pm)

(require '[clojure.pprint :refer [print-table]])

(->> (get-working-orders pm)
     (map :order)
     (map #(select-keys % [:date-created :order-id]))
     (sort-by :date-created)
     print-table
 
 )



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







