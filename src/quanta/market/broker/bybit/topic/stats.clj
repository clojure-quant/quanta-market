(ns quanta.market.broker.bybit.topic.stats)

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

 ;(topic :asset/stats "BTCUSDT")


