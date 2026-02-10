(ns quanta.market.adapter.bybit.raw
  (:require
   [taoensso.telemere :as tm]
   [missionary.core :as m]
   [tick.core :as t]
   ;[quanta.market.util.aleph :as a]
   [quanta.market.util.clj-http :refer [http-get-body-json]]
   [ta.import.helper :refer [str->double]]
   [tech.v3.dataset :as tds]
   [tablecloth.api :as tc]
   [quanta.market.adapter.bybit.normalize-request :refer [bybit-bar-params]]
   [quanta.market.barimport.bar-matcher :refer [match-window]]))

;; # Bybit api
;; The query api does NOT need credentials. The trading api does.
; https://bybit-exchange.github.io/docs/v5/announcement
;; https://www.bybit.com/
;; https://bybit-exchange.github.io/docs/spot/#t-introduction
;; https://bybit-exchange.github.io/bybit-official-api-docs/en/index.html#operation/query_symbol
;; Intervals:  1 3 5 15 30 60 120 240 360 720 "D" "M" "W" "Y"
;; limit:      less than or equal 200

(defn bybit-http-get
  "the idea is to have one http-get fn that takes care of 
   errors and result parsing"
  [segment query-params]
  ; interesting headers:
  ; {"Timenow" "1709397001926",
  ; "Ret_code" "0",
  ; "Traceid" "2b76140e45e0b2211bd94bf1b63c2a45"}
  (m/sp
   (let [r (m/? (http-get-body-json
                 (str "https://api.bybit.com/v5/market/" segment)
                 {:accept :json
                  :socket-timeout 5000
                  :connection-timeout 5000
                  :query-params query-params}))
         {:keys [retCode retMsg retExtInfo result]} r]
     (if (= retCode 0) ; the assumption is that for all requests bybit returns 0 on success
       result
       (throw (ex-info "bybit-error " ;r 
                       (select-keys r [:retCode :retMsg :retExtInfo :time])))))))

;; ASSETS

(defn get-assets [category]
  (bybit-http-get "instruments-info"
                  {:category category}))

;; BARS

(defn- convert-bar [bar]
  ;; => ["1693180800000" "26075" "26075.5" "25972.5" "25992" "6419373" "246.72884245"]
  (let [[open-time open high low close volume turnover] bar]
    {:date (-> open-time Long/parseLong t/instant)
     :open (str->double open)
     :high (str->double high)
     :low (str->double low)
     :close (str->double close)
     :volume (str->double volume)
     :turnover (str->double turnover)}))

(defn get-bars-raw
  "query-params keys:
     symbol: BTC, ....
     interval: #{ 1 3 5 15 30 60 120 240 360 720 \"D\" \"M\" \"W\" \"Y\"}  
     start: epoch-millisecond
     start: epoch-millisecond
     limit: between 1 and 200 (maximum)
   returns a missionary task with the dataset result"
  [query-params]
  (m/sp
   (->> (m/? (bybit-http-get "kline" query-params))
        (:list)
        (map convert-bar)
        (tds/->dataset)
        ;(tc/select-columns [:date :open :high :low :close :volume]))) 
        )))

(defn get-bars
  [{:keys [window] :as opts}]
  (m/sp
   (let [query-params (bybit-bar-params opts)
         bar-ds (m/? (get-bars-raw query-params))]
     (match-window bar-ds window))))





