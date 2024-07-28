(ns quanta.market.broker.bybit.topic.stats
    (:require
    [missionary.core :as m]))
  
(def test-msg-asset-stats
  {:type "snapshot"
   :topic "tickers.BTCUSDT"
   :ts 1721227632068
   :cs 34573306044,
   :data {:symbol "BTCUSDT"
          :price24hPcnt 0.0250
          :volume24h 18049.117915
          :turnover24h 1173560587.96609125
          :usdIndexPrice 65244.815694
          :prevPrice24h 63638.89
          :lowPrice24h 63592.23
          :highPrice24h 66129.54
          :lastPrice 65232.7
          }})

(defn normalize-bybit-stats [{:keys [data _type]}]
  (let [{:keys [symbol
                prevPrice24h highPrice24h lowPrice24h lastPrice
                volume24h turnover24h
                price24hPcnt usdIndexPrice]}  data]
  {:asset symbol
   :open (parse-double prevPrice24h)
   :high (parse-double highPrice24h)
   :low (parse-double lowPrice24h)
   :close (parse-double lastPrice)
   :volume (parse-double volume24h)
   :value (parse-double turnover24h)
   :change  (parse-double price24hPcnt) 
   :index (parse-double usdIndexPrice)}))


(defn transform-stats-flow [topic-data-flow]
  ; output of this flow:
  (m/eduction 
     (map normalize-bybit-stats)
     topic-data-flow))
