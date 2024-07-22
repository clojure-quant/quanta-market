(ns quanta.market.broker.bybit.msg.orderbook)

; orderbook responses: type: snapshot,delta

(def test-msg-asset-orderbook-snapshot
  {:topic "orderbook.1.BTCUSDT"
   :type "snapshot",
   :cts 1721229270063,
   :ts 1721229270067,
   :data {:s "BTCUSDT", :b [["65067.81" "0.32033"]], :seq 34575330925, :a [["65067.82" "0.156943"]], :u 33575893}})

(def test-msg-asset-orderbook-delta
  {:topic "orderbook.1.BTCUSDT"
   :type "delta"
   :cts 1721229275364
   :ts 1721229275366,
   :data {:s "BTCUSDT", :b [], :seq 34575334606, :a [["65066.62" "0.133354"]], :u 33576050}})

 ; ;(topic :asset/orderbook "1" "BTCUSDT")