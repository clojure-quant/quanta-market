(ns quanta.market.broker.bybit.topic.bars
   (:require
    [missionary.core :as m]
    [quanta.market.util :refer [split-seq-flow]]))

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

(defn normalize-bybit-bars [{:keys [_interval
                                     start end timestamp confirm
                                     open high low close volume turnover]}]
  {:open (parse-double open)
   :high (parse-double high)
   :low (parse-double low)
   :close (parse-double close)
   :volume (parse-double volume)
   :value (parse-double turnover)
   :start start
   :end end
   :timestamp timestamp
   :confirm confirm})

(defn transform-bars-flow-raw [topic-data-flow]
  ; output of this flow:
  (m/ap
   (let [{:keys [data]} (m/?> topic-data-flow)
         bar (m/?> (split-seq-flow data))]
     (when bar ; bug of split-seq-flow returns also nil.
       (normalize-bybit-bars bar)))))

(defn confirmed? [{:keys [confirm]}]
  confirm)


(defn transform-bars-flow [topic-data-flow only-finished?]
  (if only-finished?
     (m/eduction 
       (filter confirmed?)
       (transform-bars-flow-raw topic-data-flow))
     (transform-bars-flow-raw topic-data-flow)))