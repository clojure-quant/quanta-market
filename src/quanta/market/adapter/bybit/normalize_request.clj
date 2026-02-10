(ns quanta.market.adapter.bybit.normalize-request
  (:require
   [clojure.string :as str]
   [tick.core :as t] ; tick uses cljc.java-time
   [ta.calendar.validate :as cal-type]
   [quanta.market.barimport.time-helper :refer [instant->epoch-millisecond]]))

;; REQUEST CONVERSION

(defn symbol->provider
  "converts a quanta symbol to a bybit symbol

   spot or inverse:    => symbol equal
   perpetual:          => remove .P from 'symbol.P' pattern
   perpetual (USDC)    => additionally rename USDC to PERP"
  [symbol]
  ; {:keys [category] :as instrument} (db/instrument-details symbol)
  (cond
    ; USDT perp
    (str/ends-with? symbol "USDT.P")
    (str/replace symbol #"\.P$" "")

    ; USDC perp
    (str/ends-with? symbol "USDC.P")
    (str/replace symbol #"USDC\.P$" "PERP")

    :else symbol))

(defn symbol->provider-category
  "converts a quanta symbol to a bybit category"
  [symbol]
  (cond
    (or (str/ends-with? symbol "USDT.P")
        (str/ends-with? symbol "USDC.P"))
    "linear"

    (re-find #"USD$|USD[A-Z0-9]{1,2}\d\d$" symbol)
    "inverse"

    :else
    "spot"))

(def start-date-bybit (t/instant "2018-11-01T00:00:00Z"))

(def bybit-frequencies
  ; Kline interval. 1,3,5,15,30,60,120,240,360,720,D,M,W
  {:m "1"
   :m3 "3"
   :m5 "5"
   :m15 "15"
   :m30 "30"
   :h "60"
   :h2 "120"
   :h4 "240"
   :h6 "360"
   :h12 "720"
   :d "D"
   :W "W"
   :M "M"})

(defn bybit-frequency [frequency]
  (get bybit-frequencies frequency))

(defn window->bybit-window [intervals]
  {:start (-> intervals last :open instant->epoch-millisecond)
   :end (-> intervals first :open instant->epoch-millisecond)})

(defn bybit-bar-params
  "requests a window and returns a dataset with the bars.
   bybit window works with open candle time."
  [{:keys [asset calendar window] :as opts}]
  (assert asset "bybit get-bars needs asset parameter")
  (assert calendar "bybit get-bars needs calendar parameter")
  (assert window "bybit get-bars needs window parameter")
  (let [symbol-bybit (symbol->provider asset)
        category (symbol->provider-category asset)
        range-bybit (window->bybit-window window)
        f (cal-type/interval calendar)
        frequency-bybit (bybit-frequency f)]
    (assert symbol-bybit "unsupported bybit symbol")
    (assert category "unsupported bybit category")
    (assert frequency-bybit "unsupported bybit frequency")
    (merge
     {:symbol symbol-bybit
      :interval frequency-bybit
      :category category}
     range-bybit)))