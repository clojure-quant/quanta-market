(ns quanta.market.barimport.bybit.import
  (:require
   [clojure.string :as str]
   [taoensso.telemere :as tm]
   [missionary.core :as m]
   [tick.core :as t]
   [tablecloth.api :as tc]
   [quanta.market.barimport.bybit.raw :refer [get-bars-ds-normalized]]
   [quanta.market.barimport.bybit.normalize-request :refer [window->open-time to-close-time]]
   [quanta.calendar.core :refer [prior-open]]
   [ta.db.bars.protocol :refer [barsource]]))

(defn sort-ds [ds]
  (tc/order-by ds [:date] [:asc]))

;; PAGING REQUESTS

(defn failed? [bar-ds]
  (if bar-ds false true))

(defn more? [start page-size bar-ds]
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
  (tm/log! :debug (str "next-request window: " window))
  (when-not (failed? bar-ds)
    (let [earliest-received-dt (-> bar-ds tc/first :date first)
          end (prior-open calendar earliest-received-dt)
          end-instant (t/instant end)
          {:keys [start limit]} window]
      (when (more? start limit bar-ds)
        (assoc window :end end-instant)))))

(defn consolidate-datasets [datasets]
  (->> datasets
       (apply tc/concat)
       (sort-ds)))

(defn get-bars-serial
  "loads the bars sequentially in pages (1000 bars) starting from window end
   expects a window with bar open time instants"
  [{:keys [asset calendar] :as opts} {:keys [start end] :as window}]
  (tm/log! :info (str "get-bars: " (select-keys opts [:task-id :asset :calendar :import])
                      "window: " (select-keys window [:start :end])))
  (try
    (let [page-size 1000 ; 200
          ; dates need to be instant, because only instant can be converted to unix-epoch-ms
          start (if (t/instant? start) start (t/instant start))
          end (if (t/instant? end) end (t/instant end))
          window (assoc window :limit page-size :start start :end end)]
      (tm/log! :info (str "initial-page start: " start " end: " end))

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

        (loop [w window
               res []]
          (tm/log! :info (str "new page window: " (select-keys w [:start :end])))
          (if w
            (let [bar-ds (m/? (get-bars-ds-normalized opts w))]
              (recur (next-request calendar w bar-ds)
                     (conj res bar-ds)))
            (consolidate-datasets res))))
    (catch AssertionError ex
      (tm/log! :error (str   "get-bars: " calendar " assert-error: " ex)))
    (catch Exception ex
      (tm/log! :error (str "get-bars calendar: " calendar " exception: " ex)))))

(defn get-bars
  "expects a window with bar close time instants"
  [{:keys [calendar] :as opts} window]
  (let [window (window->open-time window calendar)
        ds (get-bars-serial opts window)]
    (when ds
      (tc/map-columns ds :date [:date] #(to-close-time % calendar)))))

(defrecord import-bybit []
  barsource
  (get-bars [this opts window]
    (get-bars opts window)))

(defn create-import-bybit []
  (import-bybit.))