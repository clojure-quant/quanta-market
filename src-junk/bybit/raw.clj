(ns ta.import.provider.bybit.raw
  (:require
   ;[taoensso.timbre :refer [info warn]]
   [taoensso.telemere :as tm]
   [tick.core :as t]
   [de.otto.nom.core :as nom]
   [cheshire.core :as cheshire] ; JSON Encoding
   [ta.import.helper :refer [str->double http-get]]
   [clojure.string :as str]))

;; # Bybit api
;; The query api does NOT need credentials. The trading api does.
; https://bybit-exchange.github.io/docs/v5/announcement
;; https://www.bybit.com/
;; https://bybit-exchange.github.io/docs/spot/#t-introduction
;; https://bybit-exchange.github.io/bybit-official-api-docs/en/index.html#operation/query_symbol
;; Intervals:  1 3 5 15 30 60 120 240 360 720 "D" "M" "W" "Y"
;; limit:      less than or equal 200

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

(defn- parse-history [result]
  (->> result
       (:result)
       (:list)
       (map convert-bar)))

(defn http-get-json [url query-params]
  (nom/let-nom> [res (http-get url query-params)
                 {:keys [status headers body]} res]
                (tm/log! :info
                         ;info 
                         (str "status:" status "headers: " headers))
                (cheshire/parse-string body true)))

(defn get-history-request
  "makes rest-call to binance to return bar-seq or nom-anomaly
   on error. 
   query-params keys:
   symbol: BTC, ....
   interval: #{ 1 3 5 15 30 60 120 240 360 720 \"D\" \"M\" \"W\" \"Y\"}  
   start: epoch-millisecond
   start: epoch-millisecond
   limit: between 1 and 200 (maximum)"
  [query-params]
  (tm/log! :info
           ;info 
           (str "get-history: " query-params))
  (nom/let-nom>
   [res (http-get "https://api.bybit.com/v5/market/kline" query-params)
    {:keys [status headers body]} res
    result (cheshire/parse-string body true)]
   ;(info "status: " status "headers: " headers)
   (if (= (:retCode result) 0)
     (let [bar-seq (parse-history result)]
       (when (= (count bar-seq) 0)
         (tm/log! :warn
          ;warn 
                  (str "no bybit data for params: " query-params)))
       bar-seq
       #_(if (> (count bar-seq) 0)
           bar-seq
           (nom/fail ::bybit-get-history {:message "bar-seq has count 0"
                                          :query-params query-params
                                          :result result})))
     (nom/fail ::bybit-get-history {:message "returnCode is not 0"
                                    :ret-code (:retCode result)
                                    :query-params query-params}))))

(defn get-assets [category]
  (->> (http-get-json "https://api.bybit.com/v5/market/instruments-info"
                      {:category category})
       :result
       :list
       ;(map :symbol)
       ))
(comment

  (defn get-save [category]
    (->> category
         get-assets
         (spit (str "/home/florian/repo/clojure-quant/quanta-market/resources/bybit-" category ".edn"))))

  (get-save "spot")
  (get-save "linear")
  (get-save "inverse")

  (count (get-assets "spot"))    ;; => 596
  (count (get-assets "linear"))  ;; => 432
  (count (get-assets "inverse")) ;; => 13
  (count (get-assets "option"))  ;; => 500

  (require '[clojure.string :as str])

  (->> (get-assets "spot")
       (map :symbol)
       (filter #(str/starts-with? %  "BTC")))

  (->> (get-assets "spot")
       (filter #(= "BTCUSDT" (:symbol %))))

; spot: "BTCUSDT" "BTCUSDC" 
 ; linear "BTC-02AUG24"
 ; "BTC-09AUG24" "BTC-26JUL24" "BTC-27DEC24" "BTC-27JUN25"  "BTC-27SEP24"
 ; "BTC-28MAR25" "BTC-30AUG24" "BTCPERP" "BTCUSDT"
  ; inverse
  ;BTCUSD" "BTCUSDU24" "BTCUSDZ24" 

  (->> (get-assets "linear")
       (map :symbol)
       (filter #(str/starts-with? %  "BTC")))

  (->> (get-assets "inverse")
       (map :symbol)
       (filter #(str/starts-with? %  "BTC")))

  (require '[tick.core :as t])
  (def start-date-daily (t/instant "2018-11-01T00:00:00Z"))

  (t/instant 1669852800000)
  (t/instant 1693180800000)
  (t/instant 1709673240000)

  (-> (t/instant) type)
  ;; => java.time.Instant
  (-> (t/inst) type)
  ;; => java.util.Date    WE DO NOT WANT THIS ONE!

  (-> (get-history-request
       {:symbol "BTCUSD"
        :start 1669852800000
        :interval "D"
        :category "inverse"
        :limit 3})
      (count))

  (-> (get-history-request
       {:symbol "BTCUSDT"
        :start (-> "2024-03-05T00:00:00Z" t/instant t/long (* 1000))
        :end (-> "2024-03-06T00:05:00Z" t/instant t/long (* 1000))
        :interval "1"
        :category "spot"                                  ; default linear
        :limit 3})
      count)

  ; first row is the LAST date.
  ; last row is the FIRST date
  ; if result is more than limit, then it will return LAST values first.

  ; interesting headers:
  {"Timenow" "1709397001926",
   "Ret_code" "0",
   "Traceid" "2b76140e45e0b2211bd94bf1b63c2a45"}

;
  )

