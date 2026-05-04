(ns quanta.market.adapter.eodhd.ds
  (:require
   [clojure.string :as str]
   [taoensso.timbre :refer [info warn error]]
   [missionary.core :as m]
   [tick.core :as t]
   [tech.v3.dataset :as tds]
   [tablecloth.api :as tc]
   [ta.import.helper :refer [str->double]]
   [quanta.bar.protocol :refer [barsource] :as b]
   [quanta.market.adapter.eod-close-time :refer [adjust-time-to-exchange-close]]
   [quanta.market.util.date :refer [parse-date-only]]
   [quanta.market.adapter.eodhd.raw :as eodhd]))

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

(defn sort-ds [ds]
  (tc/order-by ds [:date] [:asc]))

(defn ds-fix-date [ds]
  (tc/add-column ds :date (map parse-date-only (:date ds))))

(defn eodhd-result->dataset [calendar result]
  (-> result
      (tds/->dataset)
      (ds-fix-date)
      (adjust-time-to-exchange-close (first calendar))
      ;(sort-ds) ; 
      ;(tc/select-columns [:date :open :high :low :close :volume])
      ))

(defn error? [body]
  (-> body last :warning))

(defn get-bars-eodhd [api-token {:keys [asset calendar] :as opts} {:keys [start end] :as window}]
  (m/sp
   (let [start-str (fmt-yyyymmdd start)
         end-str (fmt-yyyymmdd end)
         r (m/? (eodhd/get-bars api-token asset start-str end-str))]
     (if-let [e (error? r)]
       (throw (ex-info "get-bars-eodhd failed" {:message e
                                                :opts opts
                                                :window window}))
       (eodhd-result->dataset calendar r)))))

(defrecord import-eodhd [api-token]
  barsource
  (get-bars [this opts window]
    (get-bars-eodhd (:api-token this) opts window)))

(defn create-import-eodhd [api-token]
  (import-eodhd. api-token))

(defn split-str->factor [s]
  (let [[left right] (str/split s #"/")]
    (/ (str->double left) (str->double right))))

; {:date "1987-06-16", :split "2.000000/1.000000"}
;(split-str->factor "2.000000/1.000000")

(defn get-splits [api-token {:keys [asset calendar]} {:keys [start end] :as _window}]
  (m/sp
   (let [start-opts (if start {:from (fmt-yyyymmdd start)} {})
         end-opts (if end {:to (fmt-yyyymmdd end)} {})
         opts (merge {:asset asset} start-opts end-opts)
         ;_ (info "get splits opts: " opts)
         splits (m/? (eodhd/get-splits api-token opts))
         ;_ (info "splits: " (count splits) "calendar: " calendar)
         ds (->> splits
                 (map #(assoc % :factor (split-str->factor (:split %))))
                 (map #(update % :date parse-date-only))
                 tc/dataset)]

     (if (> (tc/row-count ds) 0)
       (-> ds
           (adjust-time-to-exchange-close (first calendar))
           (tc/drop-columns [:split]))
       ds))))

(def eod-type-dict
  {"Common Stock" :equity
   "Preferred Stock" :equity
   "ETF" :etf
   "Mutual Fund" :fund
   "FUND" :fund
   "INDEX" :index
   "Currency" :fx
   "BOND" :bond
   "Unit" :other
   "ETC" :other
   "Warrant" :other
   "Notes" :other})

(defn convert-asset [{:keys [Code Name Type  Exchange
                             _Country _Currency _Isin]}]
  {:asset/symbol Code
   :asset/name Name
   :asset/category (get eod-type-dict Type)
   :asset/exchange Exchange
   ; Currency 
   ; Country
   ; Isin
   })

