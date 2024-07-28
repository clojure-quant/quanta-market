(ns quanta.market.broker.bybit.topic.bars)

(def test-msg-asset-bars
  {:type "snapshot",
   :topic "kline.M.BTCUSDT",
   :ts 1721227909998,
   :data [{:confirm false,
           :start 1719792000000,
           :end 1722470399999,
           :interval "M",
           :timestamp 1721227909998
           ; bar
           :open 62772.83,
           :high 66129.54,
           :low 53345.94,
           :close 65080.85,
           :volume 271788.26813,
           :turnover 16187728730.4876258,
           }]})

;  ;(topic :asset/bars "M" "BTCUSDT")