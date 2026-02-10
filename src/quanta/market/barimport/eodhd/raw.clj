(ns quanta.market.barimport.eodhd.raw
  (:require
   [clojure.set]
   [taoensso.timbre :refer [info warn error]]
   [missionary.core :as m]
   [quanta.market.util.clj-http :refer [http-get parse-json]]
   [ta.import.helper :refer [str->float]]))

;; https://eodhd.com/pricing all world EOD 20 USD/month.
;; https://eodhd.com
;; https://eodhd.com/financial-apis/api-for-historical-data-and-volumes/
;; https://eodhd.com/financial-apis/exchanges-api-list-of-tickers-and-trading-hours/
;; https://eodhd.com/financial-apis/bulk-api-eod-splits-dividends/
;; nice api and entire-market download option.
;; 15 min delayed stock prices entire market. Us stocks + fx in realtime.
;; With End-of-Day data API, we have data for more than 150 000 tickers all around the 
;; world. We cover all US stocks, ETFs, and Mutual Funds (more than 51 000 in total) 
;; from the beginning, for example, the Ford Motors data is from Jun 1972 and so on. 
;; And non-US stock exchanges we cover mostly from Jan 3, 2000.
;; FREE SUBSCRIPTIONS HAVE ACCESS TO 1 YEAR OF EOD DATA.

;; 100’000/day 1000/minute

(def base-url "https://eodhd.com/api/")

(defn eodhd-http-get
  "the idea is to have one http-get fn that takes care of 
   errors and result parsing"
  [api-token endpoint query-params]
  (m/sp
   (try (let [socket-timeout (or (:socket-timeout query-params) 5000)
              connection-timeout (or (:connection-timeout query-params) 5000)
              query-params (dissoc query-params :connection-timeout :socket-timeout)
              response (m/? (http-get
                             (str base-url endpoint)
                             {:accept :json
                              :socket-timeout socket-timeout
                              :connection-timeout connection-timeout
                              :query-params (assoc query-params
                                                   :api_token api-token
                                                   :fmt "json")}))
              body (parse-json (:body response))
              limit (get-in response [:headers "X-RateLimit-Limit"])
              remaining (get-in response [:headers "X-RateLimit-Remaining"])
              ]
          ;(println "headers: " (:headers response))
          (println "limit: " limit " remaining: " remaining)
          body)
        (catch Exception ex
          (let [data (ex-data ex)]
            ;(println "EX: " data)
            (when (= (:status data) 401) ; unauthenticated
              (throw (ex-info (:body data) data)))
            (when (= (:status data) 423)
              (throw (ex-info (:body data) data)))
            (when (= (:status data) 403)
              (throw (ex-info (:body data) data)))
            ; re-throw
            (throw ex))))))

(defn get-bars [api-token asset start-str end-str]
  (warn "getting bars asset: " asset "from: " start-str " to: " end-str)
  (let [endpoint (str "eod/" asset)]
    (eodhd-http-get
     api-token
     endpoint {:order "a"
               :period "d"
               :from start-str
               :to end-str})))

(defn get-exchanges [api-token]
  (eodhd-http-get api-token "exchanges-list/" {}))

(defn get-exchange-assets [api-token exchange-code]
  ;https://eodhd.com/api/exchange-symbol-list/{EXCHANGE_CODE}?api_token={YOUR_API_TOKEN}&fmt=json
  (eodhd-http-get api-token (str "exchange-symbol-list/" exchange-code) 
                  {:socket-timeout 15000
                   :connection-timeout 15000
                   }))

(defn get-day-bulk
  "returns bulk data for all assets of an exchange for a day 
   date is either 2010-09-21 or not included as a key which means current day
   type is either splits or dividends or nil for EOD Bars"
  [api-token {:keys [exchange type date] :as opts}]
  ; https://eodhd.com/api/eod-bulk-last-day/US?api_token={YOUR_API_TOKEN}&type=dividends
  (eodhd-http-get api-token (str "eod-bulk-last-day/" exchange)
                  (dissoc opts :exchange)))

(defn get-splits
  "returns splits for one asset
   from [optional] – start date from in the format “Y-m-d” example 2010-09-21 
   to optional – end date to in the format “Y-m-d” example 2010-09-21 "
  [api-token {:keys [asset from to] :as opts}]
  ; https://eodhd.com/api/eod-bulk-last-day/US?api_token={YOUR_API_TOKEN}&type=dividends
  (eodhd-http-get api-token (str "splits/" asset)
                  (dissoc opts :asset)))

(defn warning [result]
  (-> result last :warning))

