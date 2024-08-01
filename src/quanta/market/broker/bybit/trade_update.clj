(ns quanta.market.broker.bybit.trade-update
  (:require
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [mix]]
   ; side effects:
   [quanta.market.broker.bybit.quotefeed-category]))


(defn create-trade-update-feed [{:keys [mode]
                                 :or {mode :main}
                                 :as opts}]
  (p/create-quotefeed (merge opts
                             {:type :bybit-category
                              :mode mode
                              :segment :private})))


(defn trade-update-msg-flow [this]
  (let [trade-feed (p/get-feed this) ; bybit-subscriber
        websocket (p/get-conn trade-feed) ; websocket
        order-in (p/msg-in-flow websocket)
        order-out (p/msg-out-flow websocket)]
    (assert order-in "order-in-msg flow nil")
    (assert order-out "order-in-msg flow nil")
    (mix order-in order-out)))
