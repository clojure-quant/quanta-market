(ns quanta.market.barimport.bybit.core
  (:require
   [tick.core :as t]
   [taoensso.telemere :as tm]
   [tablecloth.api :as tc]
   [missionary.core :as m]
   [quanta.bar.protocol :as b :refer [barsource]]
   [quanta.market.barimport.bybit.raw :as raw]))

;; BE CAREFUL WHITH PARTITION.
;; partition a list of 22 items into 5 (20/4) lists of 4 items 
;; the last two items do not make a complete partition and are dropped.
;; (partition 4 (range 22))
;;=> ((0 1 2 3) (4 5 6 7) (8 9 10 11) (12 13 14 15) (16 17 18 19))

(defn partition-requests [{:keys [window] :as opts}]
  ; bybit has 1000 items limit, to be certain of no failure
  ; we only request 900 per request
  (let [opts (assoc opts :limit 200)
        windows (->> window
                     (partition 200 200 nil) ; this is important, so we get partial partitions
                     )]
    (->> windows
         (map (fn [window]
                (assoc opts :window window)))
         reverse)))

(defn create-req-tasks [opts]
  (map raw/get-bars (partition-requests opts)))

(defn limit-task [sem blocking-task]
  (m/sp
   (m/holding sem (m/? blocking-task))))

(defn consolidate [& reqs]
  {:ok (apply tc/concat (map :ok reqs))
   :missing (apply tc/concat (map :missing reqs))
   :excess (apply tc/concat (map :excess reqs))})

(defn get-bars-parallel
  "returns the concatenated dataset of each original bybit response (with bar open time)"
  [{:keys [asset calendar window] :as opts}]
  ; from: https://github.com/leonoel/missionary/wiki/Rate-limiting#bounded-blocking-execution
  ; When using (via blk ,,,) It's important to remember that the blocking thread pool 
  ; is unbounded, which can potentially lead to out-of-memory exceptions. 
  ; A simple way to work around it is by using a semaphore to rate limit the execution:
  (let [sem (m/sem 10)
        tasks (create-req-tasks opts)
        tasks-limited (map #(limit-task sem %) tasks)]
    (tm/log! (str "requesting " asset " " calendar " " window
                  " in parallel via " (count tasks) " requests .."))
    (apply m/join consolidate tasks-limited)))

(defrecord import-bybit []
  barsource
  (get-bars [this {:keys [asset calendar] :as opts} window]
    (m/sp
     (let [{:keys [missing excess ok]} (m/? (get-bars-parallel (assoc opts :window window)))]
       (tm/log! (str "bybit imported ok:" (tc/row-count ok) "missing: " (tc/row-count missing) " excess: " (tc/row-count excess)))
       ok))))

(defn create-import-bybit []
  (import-bybit.))
