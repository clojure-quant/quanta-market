(ns quanta.position.backtest.position)

(defn backtest-positions []
  (atom {:positions {}
         :trades}))

(defn order->net-position-change [{:keys [asset side qty price] :as order}]
  (case side
    :buy qty
    :sell (- 0.0 qty)
    0))

(defn enter-position [state {:keys [asset side qty price] :as order}]
  (assoc state asset {:asset asset
                      :net-qty (order->net-position-change order)}))

(defn enter-position [state {:keys [asset side qty price] :as order}]
  (assoc state asset {:asset asset
                      :net-qty (order->net-position-change order)}))
