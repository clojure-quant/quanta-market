(ns demo.asset.eodhd-market
  (:require
   [clojure.pprint :refer [print-table]]
   [tick.core :as t]
   [missionary.core :as m]
   [tablecloth.api :as tc]
   [modular.persist.edn] ; side effects to load edn files
   [modular.persist.protocol :refer [save loadr]]
   [quanta.bar.protocol :as b :refer [bardb barsource]]
   [quanta.calendar.window :refer [date-range->window
                                   window->close-range
                                   window->open-range
                                   window->intervals]]
   [quanta.market.barimport.eodhd.raw :as raw]
   [demo.env-bar :refer [eodhd eodhd-token bardb-nippy]]))

(def market-eod
  (m/? (raw/get-day-bulk eodhd-token
                         {:exchange "US"})))

market-eod

(count market-eod) ; 12215

(def stock-dict
  (->> (loadr :edn "./data/stocks.edn")
       (map (juxt :Code identity))
       (into {})))

stock-dict
(defn add-exchange-name [{:keys [code] :as row}]
  (if-let [data (get stock-dict code)]
    (assoc row :name (:Name data) :exchange (:Exchange data))
    row))

(defn short [s]
  (when s
    (subs s 0 (min 20 (dec (count s))))))

(defn in-million [n]
  (/ n 1000000.0))

(def stocks-big
  (->> market-eod
       (map #(assoc % :turnover (* (:close %) (:volume %))))
       (filter #(> (:turnover %) 1000000.0))
       (map add-exchange-name)
       (map #(update % :name short))
       (map #(update % :turnover in-million))
       (map #(update % :turnover Math/floor))
       (map #(update % :turnover long))
       (map #(select-keys % [:code :name :exchange :close :turnover]))
       (sort-by :turnover)
       (reverse)))

(count stocks-big)
; 6110
(first stocks-big)
;{:code "A", :name "Agilent Technologies", :exchange "NYSE", :close 127.5, :turnover 240}
(print-table stocks-big)
print-table

(defn import-asset [asset]
  (m/sp
   (let [bar-ds (m/? (b/get-bars eodhd
                                 {:asset asset
                                  :calendar [:us :d]}
                                 {:start (t/zoned-date-time "2000-01-01T00:00:00Z")
                                  :end (t/zoned-date-time "2026-03-20T00:00:00Z")}))]
     (m/? (b/append-bars bardb-nippy {:asset asset
                                      :calendar [:us :d]} bar-ds))
     (println "imported " asset " bars: " (tc/row-count bar-ds))
     {:asset asset :n (tc/row-count bar-ds)})))

(m/? (import-asset "MO.US"))

(defn blocking-import [asset]
  (m/? (import-asset asset)))

(->> stocks-big
     (take 1500)
     (map :code)
     (map #(str % ".US"))
     (map blocking-import)
     doall)

(def splits
  (m/? (raw/get-day-bulk eodhd-token
                         {:exchange "US"
                          :type "splits"})))

splits

[{:date "2026-02-06", :split "1.000000/25.000000", :code "ADIL", :exchange "US"}
 {:date "2026-02-06", :split "1.000000/20.000000", :code "ASST", :exchange "US"}
 {:date "2026-02-06", :split "0.100000/1.000000", :code "HNATD", :exchange "US"}
 {:date "2026-02-06", :split "0.100000/1.000000", :code "HNATF", :exchange "US"}
 {:date "2026-02-06", :split "1.000000/5.000000", :code "PRFX", :exchange "US"}
 {:date "2026-02-06", :split "1.000000/8.000000", :code "RLYB", :exchange "US"}]

(-> (date-range->window [:us :d] {:start (t/instant "2026-01-01T22:00:00Z")
                                  :end (t/instant "2026-12-31T22:00:00Z")})
    ;(window->close-range)
    ;(window->open-range)
    (window->intervals))

(defn window->date-string-vector [window]
  (->> window
       window->intervals
       :window
       (map #(-> % :open t/date str))))

(defn get-day-or-empty-vec [eodhd-token exchange date]
  (m/sp
   (try
     (m/? (raw/get-day-bulk eodhd-token
                            {:exchange "US"
                             :type "splits"
                             :date date}))
     (catch Exception ex
       (println "get-day ex: " ex)
       (println "cause: " (ex-cause ex))
       (println "ex-data: " (ex-data ex))
       []))))

(m/? (get-day-or-empty-vec eodhd-token "US" "2025-01-07"))

(defn- limit-task [sem blocking-task]
  (m/sp
   (m/holding sem (m/? blocking-task))))

(defn- run-tasks
  "runs multiple tasks"
  [tasks parallel-nr summarize]
  ; from: https://github.com/leonoel/missionary/wiki/Rate-limiting#bounded-blocking-execution
  ; When using (via blk ,,,) It's important to remember that the blocking thread pool 
  ; is unbounded, which can potentially lead to out-of-memory exceptions. 
  ; A simple way to work around it is by using a semaphore to rate limit the execution:
  (let [sem (m/sem parallel-nr)
        tasks-limited (map #(limit-task sem %) tasks)]
    (apply m/join summarize tasks-limited)))

(defn get-splits-window [eodhd-token exchange window]
  (let [days (window->date-string-vector window)
        downloads (map #(get-day-or-empty-vec eodhd-token exchange %) days)
        summarize (fn [& results] (apply concat results))]
    (run-tasks downloads 4 summarize)))

(-> (date-range->window [:us :d] {:start (t/instant "2026-01-01T22:00:00Z")
                                  :end (t/instant "2026-12-31T22:00:00Z")})
    window->date-string-vector)

(def splits-2026
  (let [window  (date-range->window [:us :d] {:start (t/instant "2026-01-01T22:00:00Z")
                                              :end (t/instant "2026-02-10T22:00:00Z")})]
    ;window->date-string-vector
    (m/? (get-splits-window eodhd-token "US" window))))

(save :edn "./data/splits-2026.edn" splits-2026)

splits-2026
(count splits-2026)

(t/date)

(def splits-2025
  (let [window  (date-range->window [:us :d] {:start (t/instant "2025-01-01T22:00:00Z")
                                              :end (t/instant "2025-12-31T22:00:00Z")})]
    ;window->date-string-vector
    (m/? (get-splits-window eodhd-token "US" window))))

(count splits-2025)
(save :edn "./data/splits-2025.edn" splits-2025)
(count splits-2025)
;; 1700
; 33000 out of 100,000
;260 2025
;30 2026
;290 * 100 = 29000

get-day-bulk