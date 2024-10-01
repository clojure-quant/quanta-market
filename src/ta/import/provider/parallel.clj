(ns ta.import.provider.parallel
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [missionary.core :as m]
   [taoensso.timbre :refer [debug info error]]
   [ta.calendar.core :refer [fixed-window]]
   [ta.db.bars.protocol :as b :refer [barsource]]
   [ta.import.provider.bybit.ds :refer [get-bars-req]]))

(defn req-window [seq]
  {:start (-> seq last t/instant)
   :end (-> seq first t/instant)})

(defn partition-requests [calendar window]
  ; bybit has 1000 items limit, to be certain of no failure
  ; we only request 900 per request
  (->> (fixed-window calendar window)
       (partition 900)
       (map req-window)))

(defn create-req-task [asset calendar window]
  ; needs to throw so it can fail.
  ; returned tasks will not be cpu intensive, so m/blk.
  (m/via m/blk
         (get-bars-req {:asset asset
                        :calendar calendar}
                       (assoc window :limit 1000))))

(defn summarize-block [b]
  {:start (-> b :date (tc/first) first)
   :end (-> b :date (tc/last) first)
   :size (-> b tc/row-count)})

(defn concat-safe [reqs]
  (try
    (->> reqs reverse (apply tc/concat))
    (catch Exception ex
      (error "could not concat parallel responses exception: " ex)
      nil)))

(defn consolidate [& reqs]
  {:blocks reqs
   :ds (concat-safe reqs)})

(defn limit-task [sem blocking-task]
  (m/sp
   (m/holding sem (m/? blocking-task))))

(defn parallel-requests [asset calendar window]
  ; from: https://github.com/leonoel/missionary/wiki/Rate-limiting#bounded-blocking-execution
  ; When using (via blk ,,,) It's important to remember that the blocking thread pool 
  ; is unbounded, which can potentially lead to out-of-memory exceptions. 
  ; A simple way to work around it is by using a semaphore to rate limit the execution:
  (let [sem (m/sem 10)
        requests (partition-requests calendar window)
        tasks (map #(create-req-task asset calendar %) requests)
        ;tasks-limited (map #(limit-task sem %) tasks)
        ]
    (info "requesting " asset " " calendar " " window
          "in parallel via " (count tasks) "requests ..")
    (m/?
     (apply m/join consolidate tasks))))

#_(defn ds->map [ds]
  ;(tc/rows :as-maps) ; this does not work, type of it is a reified dataset. 
  ; this works in repl, but when sending data to the browser it fails.
    (into []
          (tds/mapseq-reader ds)))

(defrecord import-bybit-parallel []
  barsource
  (get-bars [this {:keys [asset calendar] :as opts} window]
    (let [{:keys [blocks ds]} (parallel-requests asset calendar window)]
      ds)))

(defn create-import-bybit-parallel []
  (import-bybit-parallel.))

(comment
  (require '[tick.core :as t])
  (require '[clojure.pprint :refer [print-table]])

  (def window {:start (-> "2024-01-01T00:00:00Z" t/instant)
               :end (-> "2024-07-07T00:00:00Z" t/instant)})

  (->> (partition-requests [:crypto :m] window)
      ;(print-table)
       count)

  ; test making ONE request

  (def x (create-req-task "BTCUSDT" [:crypto :m] window))
  x
  (m/? x)

  ; test making PARALLEL requests

  (def z (parallel-requests "BTCUSDT" [:crypto :m] window))

  (:ds z)

  (require '[tech.v3.dataset.print :refer [print-range]])
  (print-range (:ds z) :all)

; 
  )
