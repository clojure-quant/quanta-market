(ns quanta.market.broker.bybit.topic)

(def topics
  {:order/execution "execution" ; ticketInfo did not work
   :order/update "order"
    ; market
   :asset/orderbook "orderbook.%s.%s" ; depth asset OK
   :asset/orderbook-top  "bookticker.%s" ; best bid ask every 100ms  NO
   :asset/bars "kline.%s.%s" ; interval asset ; OK
   :asset/stats "tickers.%s" ; OK
   :asset/trade "publicTrade.%s"  ; symbol realtime  OK
   :asset/liquidation "liquidation.%s" ; BAD
   })

(defn topic [type args]
  (if-let [s (get topics type)]
    (apply format s args)
    (throw (Exception. (ex-info "topic not found" {:type type
                                                   :args args})))))

(defn only-topic [topic]
  (fn [msg]
    (= topic (:topic msg))))

(defn get-topic-data [msg]
  (:data msg))

(comment
  (topic :asset/stats ["EURUSD"])

; 
  )