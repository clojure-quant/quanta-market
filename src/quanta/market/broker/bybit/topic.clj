(ns quanta.market.broker.bybit.topic
  (:require
   [missionary.core :as m]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.broker.bybit.topic.lasttrade :refer [transform-last-trade-flow]]
   [quanta.market.broker.bybit.topic.stats :refer [transform-stats-flow]]
   [quanta.market.broker.bybit.topic.bars :refer [transform-bars-flow]]
   [quanta.market.broker.bybit.topic.orderbook :refer [transform-book-flow]]
   [quanta.market.broker.bybit.topic.orderupdate :refer [transform-orderupdate-flow]]
   ))

(def topics
  {:order/execution "execution" 
   :order/update "order"
    ; market
   :asset/orderbook "orderbook.%s.%s" ; depth asset OK
   ;:asset/orderbook-top  "bookticker.%s" ; best bid ask every 100ms  NO
   :asset/bars "kline.%s.%s" ; interval asset ; OK
   :asset/stats "tickers.%s" ; OK
   :asset/trade "publicTrade.%s"  ; symbol realtime  OK
   :asset/liquidation "liquidation.%s" ; BAD
   })

(defn format-topic [type args]
  (if-let [s (get topics type)]
    (apply format s args)
    (throw (Exception. (ex-info "topic not found" {:type type
                                                   :args args})))))

(def bybit-intervals
  #{1 3 5 15 30 ;(min)
    60 120 240 360 720 ;(min)
    "D" ; (day)
    "W" ;(week)
    "M" ; (month)
    })


(defn format-topic-sub [{:keys [topic asset depth interval] :as sub
                         :or {topic :asset/trade}}]
  (cond
    ; 2 args (orderbook)
    (contains? #{:asset/orderbook} topic)
    (format-topic topic [depth asset])

     ; 2 args (bars)
    (contains? #{:asset/bars} topic)
    (format-topic topic [interval asset])

    ; 1 arg (asset)
    (contains? #{:asset/trade
                 :asset/stats
                 :asset/liquidation} topic)
    (format-topic topic [asset])

    ; 0 args
    (contains? #{:order/execution
                 :order/update} topic)
    (format-topic topic [])

    :else
    (throw (ex-info "unknown topic" {:topic topic}))))

(defn- only-topic [topic]
  (fn [msg]
    (= topic (:topic msg))))

(defn- get-topic-type-data [msg]
  (select-keys msg [:data :type]))

(defn topic-data-flow [msg-flow topic]
  (warn "listening to topic: " topic)
  (m/eduction
   (filter (only-topic topic))
   (map get-topic-type-data)
   msg-flow))

(defn topic-transformed-flow [topic-data-f {:keys [topic asset depth interval only-finished?] :as sub
                                            :or {topic :asset/trade
                                                 only-finished? false}}]

  (case topic
    ; quote feed
    :asset/trade (transform-last-trade-flow topic-data-f)
    :asset/stats (transform-stats-flow topic-data-f)
    :asset/bars (transform-bars-flow topic-data-f only-finished?)
    :asset/orderbook (transform-book-flow topic-data-f)
    ; orderupdates
    :order/update (transform-orderupdate-flow topic-data-f)
    topic-data-f))


(comment
  (format-topic :asset/stats ["EURUSD"])

  (contains? #{:asset/orderbook :asset/orderbook-top} :asset/orderbook)

; 
  )