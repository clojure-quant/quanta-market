(ns quanta.market.broker.bybit.task.order
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.broker.bybit.asset :refer [asset-category]]
   [quanta.market.broker.bybit.connection :as c]
   [quanta.market.precision :refer [format-price format-qty]]))


(defn order-create-msg [bybit-order]
  (wrap-header bybit-order))

(defn order-create-raw! [conn bybit-order]
  (m/sp
   (let [msg (wrap-header bybit-order)
         _ (info "order-create: " msg)
         response (m/? (c/rpc-req! conn msg))]
     response)))


(defn order-create! [conn order]
  (m/sp
   (info "order-create: " order " ..")
   (let [bybit-order (order->bybit-format order)
         response (m/? (order-create-raw! conn bybit-order))]
     (parse-order-response order response))))


(defn order-cancel! [conn order]
  (m/sp
   (info "order-cancel: " order " ..")
   (let [msg (order-cancel-msg order)
         {:keys [retCode retMsg] :as response} (m/? (c/rpc-req! conn msg))]
     (if (= 0 retCode)
       {:msg/type :cancel/confirmed
        :order order}
       {:msg/type :cancel/rejected
        :order order
        :message retMsg
        :code retCode}))))

(comment

  (asset-category "BTC.S")

  (->> 34.12345 (round2 4) double->str)
  (->> 34.12345 (round2 2) double->str)

  (def order
    {:asset "ETHUSDT"
     :side :buy
     :qty "0.01"
     :limit "1000.0"})

  (m/?  (order-create! conn order))

  (def cancel
    {:asset "ETHUSDT"
     :order-id "my-id-007"})

  (m/? (order-cancel! conn cancel))

; 
  )
