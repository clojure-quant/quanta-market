(ns quanta.market.broker.bybit.asset
  (:require
   [clojure.string :as s]))

(defn category->bybit-category [c]
  (cond
    (= c "S") "spot"
    (= c "L") "linear"
    (= c "I") "inverse"
    (= c "P") "linear" ; perpetuals are linear futures, but .P is nice too
    (= c "O") "option"
    :else "spot"))

(defn asset-category [asset]
  (let [[bybit-symbol category] (s/split asset #"\.")]
    {:bybit-symbol bybit-symbol
     :category (category->bybit-category category)}))

(comment

  (asset-category "BTCUSDT.S")
  ;; => {:bybit-symbol "BTCUSDT", :category "spot"}

  (asset-category "BTCUSDT")
  ;; => {:bybit-symbol "BTCUSDT", :category "spot"}

  (asset-category "BTCUSDT.P")
  ;; => {:bybit-symbol "BTCUSDT", :category "linear"}

;
  )