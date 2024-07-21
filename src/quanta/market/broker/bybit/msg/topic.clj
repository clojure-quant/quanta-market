(ns quanta.market.broker.bybit.msg.topic)


(defn only-topic [topic]
  (fn [msg]
    (= topic (:topic msg))))


(defn get-topic-data [msg]
  (:data msg))

