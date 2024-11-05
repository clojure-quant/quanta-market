(ns quanta.market.barimport.kibot.api
  (:require
   [clojure.string :as str]
   [missionary.core :as m]
   [taoensso.timbre :refer [info warn error]]
   [clojure.java.io :as io]
   [tick.core :as t]
   [tech.v3.dataset :as tds]
   [tech.v3.datatype.argops :as argops]
   [tablecloth.api :as tc]
   [de.otto.nom.core :as nom]
   [ta.calendar.validate :as cal-type]
   [quanta.market.asset.db :as db]
   [ta.db.bars.protocol :refer [barsource]]
   [ta.import.helper :refer [p-or-fail]]
   [quanta.market.barimport.kibot.helper :refer [adjust-time-to-exchange-close]]
   [quanta.market.barimport.time-helper :refer [window->open-time]]
   [quanta.market.barimport.kibot.raw :as kb]))

(defn string->stream [s]
  (io/input-stream (.getBytes s "UTF-8")))

(defn kibot-result->dataset [exchange-kw csv]
  (-> (tds/->dataset (string->stream csv)
                     {:file-type :csv
                      :header-row? false
                      :dataset-name "kibot-bars"})
      (tc/rename-columns {"column-0" :date
                          "column-1" :open
                          "column-2" :high
                          "column-3" :low
                          "column-4" :close
                          "column-5" :volume})
      (adjust-time-to-exchange-close exchange-kw)))

(def category-mapping
  {:equity "stocks"
   :etf "ETF"
   :future "futures"
   :fx "forex"})

(defn symbol->provider [asset]
  (let [{:keys [category kibot] :as instrument} (db/instrument-details asset)
        type (get category-mapping category)
        asset (if kibot kibot asset)]
    {:type type
     :symbol asset}))

(def interval-mapping
  {:d "daily"})

(defn fmt-yyyymmdd [dt]
  (t/format (t/formatter "YYYY-MM-dd") (t/date-time dt)))

(defn make-one [range key kibot-name]
  (if-let [dt (key range)]
    (into {} [[kibot-name (fmt-yyyymmdd dt)]])
    {}))

(defn start-end->kibot [{:keys [start] :as range}]
  ; {:startdate "2023-09-01" :enddate "2024-01-01"}
  (merge (make-one range :start :startdate)
         (make-one range :end :enddate)))

(defn range->parameter [{:keys [start] :as range}]
  (cond
    (= range :full)
    {:period 100000}

    (int? range)
    {:period range}

    :else
    (start-end->kibot range)))

(defn get-bars [api-key {:keys [asset calendar] :as opts} window]
  (m/sp
   (let [window (-> (select-keys window [:start :end])
                    (window->open-time calendar))
         _ (info "get-bars kibot " asset " " calendar " " window " ..")
         symbol-map (symbol->provider asset)
         _ (assert symbol-map (str "kibot symbol not found: " asset))
         f (cal-type/interval calendar)
         _ (assert f "kibot get-bars needs :calendar")
         exchange (cal-type/exchange calendar)
         period-kibot (get interval-mapping f)
         _ (assert period-kibot (str "kibot does not support frequency: " f))
         range-kibot (range->parameter window)
         _ (info "kibot make request interval: " period-kibot " range: " range-kibot "asset-kibot: " symbol-map)
         result (m/? (kb/history api-key (merge symbol-map
                                                range-kibot
                                                {:interval period-kibot
                                                 :timezone "UTC"
                                                 :splitadjusted 1})))
         ds (kibot-result->dataset exchange result)]
     ds)))

(defrecord import-kibot [api-key]
  barsource
  (get-bars [_ opts window]
    (get-bars api-key opts window)))

(defn create-import-kibot [api-key]
  (import-kibot. api-key))

(defn symbols->str [symbols]
  (->> (interpose "," symbols)
       (apply str)))

(defn provider->symbol [provider-symbol]
  (if-let [inst (db/get-instrument-by-provider :kibot provider-symbol)]
    (:symbol inst)
    provider-symbol))

(comment

  (def api-key {:user "guest" :password "guest"})

  (def csv "09/01/2023,26.73,26.95,26.02,26.1,337713\r\n")
  (def csv
    (kibot/history api-key {:symbol "SIL" ; SIL - ETF
                            :interval "daily"
                            :period 1
                            :type "ETF" ; Can be stocks, ETFs forex, futures.
                            :timezone "UTC"
                            :splitadjusted 1}))
  csv

  (-> (kibot-result->dataset :us csv)
      (tc/info :columns))

  (db/instrument-details "EUR/USD")

  (symbol->provider "MES0")
  (symbol->provider "EUR/USD")
  ;; => {:type "futures", :symbol "ES"}
  (provider->symbol "ES")

  (symbols->str ["MSFT" "ORCL"])
  (symbols->str ["ES"])

  (start-end->kibot {:start (t/inst)})
  (start-end->kibot {:end (t/inst)})
  (start-end->kibot {:start (t/inst) :end (t/inst)})

; 
  )
