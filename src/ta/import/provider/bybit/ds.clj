(ns ta.import.provider.bybit.ds
  (:require
   [clojure.string :as str]
   ;[taoensso.timbre :refer [debug info error]]
   [taoensso.telemere :as tm]
   [de.otto.nom.core :as nom]
   [tick.core :as t] ; tick uses cljc.java-time
   [tech.v3.dataset :as tds]
   [tablecloth.api :as tc]
   [ta.import.provider.bybit.raw :as bybit]
   [ta.calendar.validate :as cal-type]
   [ta.calendar.core :refer [prior-close]]
   [ta.db.bars.protocol :refer [barsource]]))

;; RESPONSE CONVERSION

(defn sort-ds [ds]
  (tc/order-by ds [:date] [:asc]))

(defn bybit-result->dataset [result]
  (if (empty? result)
    (tds/->dataset result)
    (-> result
        (tds/->dataset)
        (sort-ds) ; bybit returns last date in first row.
        (tc/select-columns [:date :open :high :low :close :volume]))))

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
   :m15 "15"
   :m30 "30"
   :h "60"
   :d "D"})

(defn bybit-frequency [frequency]
  (get bybit-frequencies frequency))

(defn instant->epoch-millisecond [dt]
  (-> dt
      (t/long)
      (* 1000)))

(defn range->parameter [window]
  (assoc window
         :start (instant->epoch-millisecond (:start window))
         :end (instant->epoch-millisecond (:end window))))

(defn get-bars-req [{:keys [asset calendar] :as opts} window]
  (tm/log! :debug
    (str
           "get-bars-req: " (select-keys opts [:task-id :asset :calendar :import])
           "window: "  (select-keys window [:start :end])))
  
  (assert asset "bybit get-bars needs asset parameter")
  ;(assert calendar "bybit get-bars needs calendar parameter")
  (assert window "bybit get-bars needs window parameter")
  (nom/let-nom>
   [f (if calendar
        (cal-type/interval calendar)
        (nom/fail ::get-bars-req {:message "bybit get-bars needs :calendar"}))
    frequency-bybit (bybit-frequency f)
    frequency-bybit (if frequency-bybit
                      frequency-bybit
                      (nom/fail ::get-bars-req {:message "unsupported bybit frequency!"
                                                :opts opts
                                                :range window}))
    symbol-bybit (symbol->provider asset)
    category (symbol->provider-category asset)
    range-bybit (range->parameter window)
    response (bybit/get-history-request (merge
                                         {:symbol symbol-bybit
                                          :interval frequency-bybit
                                          :category category}
                                         range-bybit))]
   (try
     (bybit-result->dataset response)
     (catch Exception ex
       (tm/log! :error
        ;error 
                (str
                 "could not convert bybit bar response to dataset "
              " asset: " asset " calendar: " calendar
              " window: " window
              " response: " response
              " ex: " ex))
       nil))))

;; PAGING REQUESTS

(defn failed? [bar-ds]
  (if bar-ds false true))

(defn more? [start page-size bar-ds]
  ;(info "more start: " start " page-size " page-size "bars: " bar-ds)
  (cond
    (failed? bar-ds) false

    (and (= page-size (tc/row-count bar-ds))
         (t/> (-> bar-ds tc/first :date first)
              start))
    true

    :else
    false))

(defn next-request
  "returns the parameters for the next request.
   returns nil if last result is an anomaly, or
   if no more requests are needed."
  [calendar window bar-ds]
  (tm/log!  :debug ;debug 
   (str "next-request window: " window))
  (when-not (nom/anomaly? bar-ds)
    (let [earliest-received-dt (-> bar-ds tc/first :date first)
          [calendar-kw interval-kw] calendar
          end (prior-close calendar-kw interval-kw earliest-received-dt)
          end-instant (t/instant end)
          {:keys [start limit]} window]
      (when (more? start limit bar-ds)
        (assoc window :end end-instant)))))

(defn all-ds-valid [datasets]
  (let [or-fn (fn [a b] (or a b))]
    (->> (map nom/anomaly? datasets)
         (reduce or-fn false)
         not)))

(defn set-close-time [dt]
  (-> dt
      (t/date)
      (t/at (t/time "23:59:59"))
      (t/in "UTC")
      (t/instant)))

(defn set-close-time-vec [dt-vec]
  (map set-close-time dt-vec))

(defn set-daily-time [{:keys [asset calendar] :as opts} ds]
  (let [frequency (second calendar)]
    (if (= frequency :d)
      (tc/update-columns ds {:date set-close-time-vec})
      ds)))

(defn consolidate-datasets [opts window datasets]
  (if (all-ds-valid datasets)
    (->> datasets
         (apply tc/concat)
         (sort-ds)
         (set-daily-time opts))
    (nom/fail ::consolidate-datasets {:message "paged request failed!"
                                      :opts opts
                                      :range window})))

(defn get-bars [{:keys [asset calendar] :as opts} {:keys [start end] :as window}]
  (tm/log! :info
   ;info 
   (str 
           "get-bars: " (select-keys opts [:task-id :asset :calendar :import])
        "window: " (select-keys window [:start :end])))
  (try
    (let [page-size 1000 ; 200
        ; dates need to be instant, because only instant can be converted to unix-epoch-ms
          start (if (t/instant? start) start (t/instant start))
          end (if (t/instant? end) end (t/instant end))
          window (assoc window :limit page-size :start start :end end)]
      (tm/log! :info
       ;info 
               (str "initial-page start: " start " end: " end))
      (->> (iteration (fn [window]
                        (tm/log! :info 
                                 ;info 
                                 (str "new page window: " (select-keys window [:start :end])))
                        (get-bars-req opts window))
                      :initk window
                      :kf  (partial next-request calendar window))
           (consolidate-datasets opts window)))
    (catch AssertionError ex
      (tm/log! :error
       ;error 
             (str   "get-bars: " calendar " assert-error: " ex))
      (nom/fail ::compress {:message "assert-error in compressing ds-higher"
                            :opts opts
                            :range window}))
    (catch Exception ex
      (tm/log! :error 
               ;error 
               (str "get-bars calendar: " calendar " exception: " ex))
      (nom/fail ::compress {:message "exception in bybit get-bars"
                            :opts opts
                            :range window}))))

(defrecord import-bybit []
  barsource
  (get-bars [this opts window]
    (get-bars opts window)))

(defn create-import-bybit []
  (import-bybit.))

(comment

  (type (create-import-bybit))
  (bybit-frequency :d)
  (bybit-frequency :h)
  (bybit-frequency :m)
  (bybit-frequency :s)

  (def ds (tc/dataset [{:date (t/instant)}
                       {:date (t/instant)}]))

  (-> (t/instant) instant->epoch-millisecond)

  (get-bars {:asset "BTCUSDT"
             :calendar [:crypto :d]}
            {:start (t/instant "2024-02-26T00:00:00Z")})

  (-> (get-bars-req
       {:asset "BTCUSDT"
        :calendar [:crypto :m]}
       {:start (-> "2024-03-05T00:00:00Z" t/instant)
        :end (-> "2024-03-06T00:00:00Z" t/instant)})
      (tc/last)
      :date
      first
      ;count
      )
     ; 2024-03-05T21:26:00Z

  (all-ds-valid [1 2 3 4 5])
  (all-ds-valid [1 2 3 (nom/fail ::asdf {}) 4 5])

  (get-bars
   {:asset "BTCUSDT"
    :calendar [:crypto :m]}
   {:start (-> "2024-02-29T00:00:00Z" t/instant)
    :end (-> "2024-02-29T00:07:00Z" t/instant)})

  (-> (get-bars
       {:asset "BTCUSDT"
        :calendar [:crypto :d]}
       {:start (-> "2021-07-04T00:00:00Z" t/instant)
        :end (-> "2024-05-02T00:00:00Z" t/instant)})
      (tc/write-csv! "/clojure-quant/quanta/lib/indicator/test/ta/indicator/csv/BYBIT_SPOT_BTCUSDT_1D.csv"))
; 
  )