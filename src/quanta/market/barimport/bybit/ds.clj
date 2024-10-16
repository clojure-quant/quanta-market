(ns quanta.market.barimport.bybit.ds
  (:require
   [clojure.string :as str]
   [taoensso.telemere :as tm]
   [de.otto.nom.core :as nom]
   [tick.core :as t] ; tick uses cljc.java-time
   [tech.v3.dataset :as tds]
   [tablecloth.api :as tc]
   [ta.import.provider.bybit.raw :as bybit]
   [ta.import.helper :refer [expected-bars calendar-seq-prior-open]]

   [quanta.calendar.core :refer [prior-open]]
   [ta.db.bars.protocol :refer [barsource]]))

(defn get-bars-req
  "requests a window and returns a dataset with the bars.
   bybit window works with open candle time."
  [{:keys [asset calendar] :as opts} window]
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
          end (prior-open calendar earliest-received-dt)
          end-instant (t/instant end)
          {:keys [start limit]} window]
      (when (more? start limit bar-ds)
        (assoc window :end end-instant)))))

(defn all-ds-valid [datasets]
  (let [or-fn (fn [a b] (or a b))]
    (->> (map nom/anomaly? datasets)
         (reduce or-fn false)
         not)))

(defn consolidate-datasets [opts window datasets]
  (if (all-ds-valid datasets)
    (->> datasets
         (apply tc/concat)
         (sort-ds))
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

      ; NOTE: this code makes more requests than needed.
      ;       for 1 day (1440 minute candles) 2 requests would be needed, but 4 are done
      ;(->> (iteration (fn [window]
      ;                  (tm/log! :info
      ;                           ;info
      ;                           (str "new page window: " (select-keys window [:start :end])))
      ;                  (get-bars-req opts window))
      ;                :initk window
      ;                :kf  (partial next-request calendar window))
      ;     (consolidate-datasets opts window))

      (->> (loop [w window
                  res []]
             (if w
               (do
                 (tm/log! :info
                          (str "new page window: " (select-keys w [:start :end])))
                 (let [bar-ds (get-bars-req opts w)
                       received-c (tc/row-count bar-ds)
                       expected-c (expected-bars calendar w page-size)]
                   (tm/log! (if (= received-c expected-c) :info :warn)
                            (str "page result: " (select-keys w [:start :end])
                                 ", expected bars: " expected-c
                                 ", received bars: " received-c))
                   (recur (next-request calendar w bar-ds)
                          (conj res bar-ds))))
               res))
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

  (get-bars {:asset "BTCUSDT"
             :calendar [:crypto :m]}
            {:start (t/instant "2024-10-01T00:00:00Z")
             :end (t/instant "2024-10-01T23:59:00Z")})

  (all-ds-valid [1 2 3 4 5])
  (all-ds-valid [1 2 3 (nom/fail ::asdf {}) 4 5])

; 
  )