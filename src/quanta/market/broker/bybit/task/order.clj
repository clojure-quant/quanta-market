(ns quanta.market.broker.bybit.task.order
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [clojure.string :as s]
   [quanta.market.broker.bybit.connection :as c]
   [quanta.market.precision :refer [format-price format-qty]]
   ))

 (defn category->bybit-category [c]
   (cond
     (= c "S") "spot"
     (= c "L") "linear"
     (= c "P") "perpetual"
     (= c "O") "option"
     :else "spot"))

(defn asset-category [asset]
  (let [[bybit-symbol category] (s/split asset #"\.")]
    {:bybit-symbol bybit-symbol 
     :category (category->bybit-category category)}))
 
(def order-spot-limit-tp-sl 
; Spot Limit order with market tp sl
{"category" "spot",
 "symbol" "BTCUSDT",
 "side" "Buy",
 "orderType" "Limit",
 "qty" "0.01",
 "price" "28000",
 "timeInForce" "PostOnly",
 "takeProfit" "35000",
 "stopLoss" "27000",
 "tpOrderType" "Market",
 "slOrderType" "Market"}  
  )


; Spot Limit order with limit tp sl
;{"category": "spot","symbol": "BTCUSDT","side": "Buy","orderType": "Limit","qty": "0.01","price": "28000","timeInForce": "PostOnly","takeProfit": "35000","stopLoss": "27000","tpLimitPrice": "36000","slLimitPrice": "27500","tpOrderType": "Limit","slOrderType": "Limit"}

;// Spot PostOnly normal order
;{"category":"spot","symbol":"BTCUSDT","side":"Buy","orderType":"Limit","qty":"0.1","price":"15600","timeInForce":"PostOnly","orderLinkId":"spot-test-01","isLeverage":0,"orderFilter":"Order"}

;// Spot TP/SL order
;{"category":"spot","symbol":"BTCUSDT","side":"Buy","orderType":"Limit","qty":"0.1","price":"15600","triggerPrice": "15000", "timeInForce":"Limit","orderLinkId":"spot-test-02","isLeverage":0,"orderFilter":"tpslOrder"}

;// Spot margin normal order (UTA)
;{"category":"spot","symbol":"BTCUSDT","side":"Buy","orderType":"Limit","qty":"0.1","price":"15600","timeInForce":"Limit","orderLinkId":"spot-test-limit","isLeverage":1,"orderFilter":"Order"}

;// Spot Market Buy order, qty is quote currency
;{"category":"spot","symbol":"BTCUSDT","side":"Buy","orderType":"Market","qty":"200","timeInForce":"IOC","orderLinkId":"spot-test-04","isLeverage":0,"orderFilter":"Order"}

(defn type->bybit [ordertype]
  (case ordertype
    :limit "Limit"
    :market "Market"
    "Market"))


(defn tif->bybit [tif]
  (case tif
    ; "PostOnly"
    :gtc "GTC" ; Good till canceled (GTC)
    :fok "FOK" ; Fill or Kill (FOK) 
    :ioc "IOC")) ; Immediate or Cancel (IOC)

;(type->bybit :limit)
;(type->bybit :market)

(defn wrap-header [bybit-order]
  {"op" "order.create"
   "header" {"X-BAPI-TIMESTAMP" (System/currentTimeMillis)
             "X-BAPI-RECV-WINDOW" "8000"
             "Referer" "bot-001" ; for api broker
             }
   "args" [bybit-order]})

(defn order->bybit-format [{:keys [order-id asset side qty limit ordertype tif]}]
  (let [{:keys [bybit-symbol category]} (asset-category asset)
        order-id-map (if order-id 
                       {"orderLinkId" order-id} ; max 36 chars numbers/letters(upper/lower) dashes underscores
                       {})
        limit-price-map (if limit 
                          {"price" (format-price asset limit)}
                          {})
         tif-map (if tif
                    {"timeInForce" (tif->bybit tif)}
                       {})]
  (merge order-id-map 
         limit-price-map
         tif-map
         {"symbol" bybit-symbol
          "category" category 
          "side" (case side
                    :buy "Buy"
                    :sell "Sell")
          "orderType" (type->bybit ordertype) ; "Limit"
          "qty" (format-qty asset qty)}
            )))

(def order-response-failed-example
  {:retCode 110007,
   :retMsg "ab not enough for new order",
   :connId "cpv85t788smd5eps8ncg-2tgm",
   :op "order.create",
   :header {:Timenow 1721152638764,
            :X-Bapi-Limit-Status 9,
            :X-Bapi-Limit-Reset-Timestamp 1721152638762,
            :Traceid "2bc167807ee719a474416104ae7e964b",
            :X-Bapi-Limit 10},
   :reqId "5bY1PVT-"
   :data {}})

(def order-response-failed-example2
{:retCode 10005,
 :retMsg "Permission denied for current apikey",
 :connId "cpv86i6c0hvd5nkl25n0-2v79",
 :op "order.create",
 :reqId "XiMTlhQV"})

(def order-response-failed-example3
{:op "order.create",
 :success false, 
 :conn_id "cq1814tdaugt75sdcg8g-2ljtq",
 :ret_msg "Params Error"})


(defn order-create-raw! [conn bybit-order]
  (m/sp
   (let [msg (wrap-header bybit-order)
         _ (info "order-create: " msg)
         response (m/? (c/rpc-req! conn msg))]
     response)))


(defn parse-order-response [order {:keys [success retCode ret_msg retMsg] :as response}]
  (if (or success (= 0 retCode))
    {:msg/type :order/confirmed
     :order order}
    {:msg/type :order/rejected
     :order order
     :message (or retMsg ret_msg)
     :code retCode}))

(defn order-create! [conn order]
  (m/sp
    (info "order-create: " order " ..")
   (let [bybit-order (order->bybit-format order)
         response (m/? (order-create-raw! conn bybit-order))]
     (parse-order-response order response)
     )))



(defn order-cancel-msg [{:keys [asset order-id]}]
  {"op" "order.cancel"
   "header" {"X-BAPI-TIMESTAMP" (System/currentTimeMillis)
             "X-BAPI-RECV-WINDOW" "8000"
             "Referer" "bot-001" ; for api broker
             }
   "args" [{"category" "linear" ; product type: spot, linear, inverse, option
            "symbol" asset
            ;"orderId" order-id ; bybit order id
            "orderLinkId" order-id ; user order id
            }]})

(def order-cancel-failed-example 
{:retCode 10005,
 :retMsg "Permission denied for current apikey",
 :connId "cpv86i6c0hvd5nkl25n0-2v7a",
 :op "order.cancel",
 :reqId "XMrZJoEs"}  
  )

(def order-cancel-failed-example2
{:retCode 110001,
 :retMsg "order not exists or too late to cancel",
 :connId "cpv86i6c0hvd5nkl25n0-2v7a",
 :op "order.cancel",
 :header
 {:Timenow "1721243099025",
  :X-Bapi-Limit-Status "9",
  :X-Bapi-Limit-Reset-Timestamp "1721243099024",
  :Traceid "0313d8af73592c2034d873ef1f6479ac",
  :X-Bapi-Limit "10"},
 :reqId "-Cquvtz7",
 :data {}})

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
