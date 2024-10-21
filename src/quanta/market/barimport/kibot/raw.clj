(ns quanta.market.barimport.kibot.raw
  (:require
   [taoensso.telemere :as tm]
   [missionary.core :as m]
   [clj-commons.byte-streams :as bs]
   [quanta.market.util.aleph :as a]))

; dividends/splits:
; Request URL
; http://api.kibot.com?action=adjustments&symbol=[symbol]&startdate=[startdate]&enddate=[enddate]&splitsonly=[splitsonly]&dividendsonly=[dividendsonly]&symbolsonly=[symbolsonly]
;
; Response
;The server returns TAB separated values with the first line defining the fields and their order. Here is an example:
; Date Symbol Company Action Description
; 2/16/2010 MSFT Microsoft Corp. 0.1300 Dividend
; 5/18/2010 MSFT Microsoft Corp. 0.1300 Dividend

(defn extract-error [body]
  (when-let [match (re-matches #"^(\d\d\d)\s([\w\s]+)\.([\w\s\.\'/,:]+)" body)]
    (let [[_ error-code error-type error-message] match]
      {:code error-code
       :type error-type
       :message error-message})))

(def base-url "http://api.kibot.com/")

(defn head-request
  [opts]
  (m/sp
   (let [base-url "http://api.kibot.com/"

         _ (tm/log! (str "kibot head url: " base-url " opts: " opts))
         response (m/? (a/http-head base-url opts))
         ;{:keys [headers body]} response
         ;_ (tm/log! (str "kibot head headers: " headers))
         ]
     response)))

(defn make-request-raw
  [query-params]
  (m/sp
   (let [base-url "http://api.kibot.com/"
         opts {:query-params query-params}
         _ (tm/log! (str "kibot get-raw url: " base-url " opts: " opts))
         response (m/? (a/http-get base-url opts))
         {:keys [headers body]} response
         _ (tm/log! (str "kibot api headers: " headers))]
     response)))

(defn make-request
  [query-params]
  (m/sp
   (let [opts {:query-params query-params}
         _ (tm/log! (str "kibot get url: " base-url " opts: " opts))
         response (m/? (a/http-get base-url opts))
         {:keys [headers body]} response
         _ (tm/log! (str "kibot api headers: " headers))
         body-str (bs/to-string body)
         kibot-error (extract-error body-str)]
     (if kibot-error
       (do
         (tm/log! (str "kibot request error: " (merge kibot-error query-params)))
         (throw (ex-info (:message kibot-error) (merge kibot-error query-params))))
       body-str))))

; http://api.kibot.com?action=login&user=guest&password=guest

(defn login [{:keys [user password] :as api-key}]
  (m/sp
   (let [result (m/? (make-request {:action "login"
                                    :user user
                                    :password password}))]
     (tm/log! (str "kibot login result: \r\n" result))
     result)))

(defn status []
  (make-request {:action "status"}))

(defn history [api-key opts]
  (let [{:keys [user password]} api-key]
    (make-request (merge
                   opts
                   {:action "history"
                    :user user
                    :password password}))))

(defn splits
  [{:keys [user password]} opts]
  (make-request (merge
                 {:action "adjustments"
                  :user user
                  :password password}
                 opts)))

(defn snapshot [{:keys [user password]} opts]
  (make-request (merge
                 {:action "snapshot"
                  :user user
                  :password password}
                 opts)))

; This example will work even if you do not have a subscription:
; http://api.kibot.com/?action=snapshot&symbol=$NDX,AAPL
; return format: Symbol,Date,Time,LastPrice,LastVolume,Open,High,Low,Close,Volume,ChangePercent,TimeZone.








