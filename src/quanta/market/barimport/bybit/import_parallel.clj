(ns quanta.market.barimport.bybit.import-parallel
  (:require
   [tick.core :as t]
   [taoensso.telemere :as tm]
   [tablecloth.api :as tc]
   [missionary.core :as m]
   [ta.calendar.core :refer [fixed-window]]
   [ta.db.bars.protocol :as b :refer [barsource]]
   [quanta.market.barimport.bybit.raw :as bb]
   [quanta.market.barimport.bybit.normalize-request :refer [bybit-bar-params]]))

(defn req-window [seq]
  {:start (-> seq last t/instant)
   :end (-> seq first t/instant)})

;; BE CAREFUL WHITH PARTITION.
;; partition a list of 22 items into 5 (20/4) lists of 4 items 
;; the last two items do not make a complete partition and are dropped.
;; (partition 4 (range 22))
;;=> ((0 1 2 3) (4 5 6 7) (8 9 10 11) (12 13 14 15) (16 17 18 19))

(defn partition-requests [calendar window]
  ; bybit has 1000 items limit, to be certain of no failure
  ; we only request 900 per request
  (->> (fixed-window calendar window)
       (partition 900 900 nil) ; this is important, so we get partial partitions
       (map req-window)
       (into [])))

(defn create-req-task [opts window]
  ; needs to throw so it can fail.
  ; returned tasks will not be cpu intensive, so m/blk.
  (m/via m/blk
         (tm/log! (str "request-block " window))
         (let [query-params  (bybit-bar-params opts window)
               query-params (assoc query-params :limit 1000)]
           (m/? (bb/get-bars-ds query-params)))))

(defn summarize-block [b]
  {:start (-> b :date (tc/first) first)
   :end (-> b :date (tc/last) first)
   :size (-> b tc/row-count)})

(defn concat-safe [reqs]
  (try
    (->> reqs reverse (apply tc/concat))
    (catch Exception ex
      (println "could not concat parallel responses exception: " ex)
      nil)))

(defn consolidate [& reqs]
  {:blocks reqs
   :ds (concat-safe reqs)})

(defn limit-task [sem blocking-task]
  (m/sp
   (m/holding sem (m/? blocking-task))))

(defn parallel-requests [{:keys [asset calendar] :as opts} window]
  ; from: https://github.com/leonoel/missionary/wiki/Rate-limiting#bounded-blocking-execution
  ; When using (via blk ,,,) It's important to remember that the blocking thread pool 
  ; is unbounded, which can potentially lead to out-of-memory exceptions. 
  ; A simple way to work around it is by using a semaphore to rate limit the execution:
  (let [sem (m/sem 10)
        requests (partition-requests calendar window)
        tasks (map #(create-req-task opts %) requests)
        ;tasks-limited (map #(limit-task sem %) tasks)
        ]
    (tm/log! (str "requesting " asset " " calendar " " window
                  "in parallel via " (count tasks) "requests .."))
    (m/?
     (apply m/join consolidate tasks))))

(defrecord import-bybit-parallel []
  barsource
  (get-bars [this {:keys [asset calendar] :as opts} window]
    (let [{:keys [blocks ds]} (parallel-requests opts window)]
      ds)))

(defn create-import-bybit-parallel []
  (import-bybit-parallel.))
