(ns quanta.market.barimport.eodhd.ds
  (:require
   [missionary.core :as m]
   [taoensso.timbre :refer [info warn]]
   [tick.core :as t] ; tick uses cljc.java-time
   [tech.v3.dataset :as tds]
   [tablecloth.api :as tc]
   [quanta.bar.protocol :refer [barsource] :as b]
   [quanta.market.util.date :refer [parse-date-only]]
   [quanta.market.barimport.eodhd.raw :as eodhd]))

(def eodhd-frequencies
  {:m "1"
   :h "60"
   :d "d"})

(def yyyy-mm-dd-formatter
  (t/formatter "YYYY-MM-dd"))

(defn fmt-yyyymmdd [dt]
  (let [dt (if (t/instant? dt)
             (t/zoned-date-time dt)
             dt)]
    (t/format yyyy-mm-dd-formatter dt)))

(defn convert-date [dt-s]
  (-> (parse-date-only dt-s)
      (t/at (t/time "00:00:00"))
      (t/in "UTC")
      (t/instant)))

(defn sort-ds [ds]
  (tc/order-by ds [:date] [:asc]))

(defn ds-fix-date [ds]
  (tc/add-column ds :date (map convert-date (:date ds))))

(defn eodhd-result->dataset [result]
  (-> result
      (tds/->dataset)
      (ds-fix-date)
      ;(sort-ds) ; bybit returns last date in first row.
      ;(tc/select-columns [:date :open :high :low :close :volume])
      ))

(defn error? [body]
  (-> body last :warning))

(defn get-bars-eodhd [api-token {:keys [asset calendar] :as opts} {:keys [start end] :as window}]
  (m/sp
   (let [start-str (fmt-yyyymmdd start)
         end-str (fmt-yyyymmdd end)
         r (m/? (eodhd/get-bars api-token asset start-str end-str))]
     (warn "r: " r)
     (if-let [e (error? r)]
       (throw (ex-info "get-bars-eodhd failed" {:message e
                                                :opts opts
                                                :window window}))
       (eodhd-result->dataset r)))))

(defrecord import-eodhd [api-token]
  barsource
  (get-bars [this opts window]
    (get-bars-eodhd (:api-token this) opts window)))

(defn create-import-eodhd [api-token]
  (import-eodhd. api-token))
