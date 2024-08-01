(ns quanta.market.broker.bybit.order.create
  (:require
   [quanta.market.broker.bybit.asset :refer [asset-category]]
   [quanta.market.precision :refer [format-price format-qty]]))

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
            "qty" (format-qty asset qty)
            "marketUnit" "baseCoin" ; hack for market buy orders
            })))

(defn order-create-msg [order]
   (let [bybit-order (order->bybit-format order)]
      (wrap-header bybit-order)))





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
   "slOrderType" "Market"})

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

