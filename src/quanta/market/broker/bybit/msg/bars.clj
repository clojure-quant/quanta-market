(ns quanta.market.broker.bybit.msg.bars)


(def test-msg-asset-bars
  {:type "snapshot",
   :topic "kline.M.BTCUSDT",
   :ts 1721227909998,
   :data [{:confirm false,
           :open 62772.83,
           :turnover 16187728730.4876258,
           :start 1719792000000,
           :close 65080.85,
           :volume 271788.26813,
           :high 66129.54,
           :low 53345.94,
           :interval "M",
           :end 1722470399999,
           :timestamp 1721227909998}]})

;  ;(topic :asset/bars "M" "BTCUSDT")