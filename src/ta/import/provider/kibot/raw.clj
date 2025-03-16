(ns ta.import.provider.kibot.raw
  (:require
   [clojure.string :as str]
   [clojure.set]
   [taoensso.timbre :refer [info warn error]]
   [clojure.edn :as edn]
   [clj-http.client :as http]
   [cheshire.core :as cheshire] ; JSON Encoding
   [de.otto.nom.core :as nom]
   [ta.import.helper :refer [str->float http-get]]
   [throttler.core]))

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
  (if-let [match (re-matches #"^(\d\d\d)\s([\w\s]+)\.([\w\s\.\'/,:]+)" body)]
    (let [[_ error-code error-type error-message] match]
      (nom/fail ::kibot-request
                {:code error-code
                 :type error-type
                 :message error-message}))
    nil))

(def base-url "http://api.kibot.com")

(defn make-request [query-params]
  (nom/let-nom> [result (http-get base-url query-params)
                 body (:body result)
                 ;kibot-error (extract-error body)
                 ]
     ;(info "kibot response status: " (:status result))           
                body))

; http://api.kibot.com?action=login&user=guest&password=guest

(defn login [api-key]
  (let [{:keys [user password]} api-key]
    (info "login user: " user)
    (make-request  {:action "login"
                    :user user
                    :password password})))

(defn status []
  (make-request {:action "status"}))

(defn history [api-key opts]
  (let [{:keys [user password]} api-key]
    ;(info "login user: " user "pwd: " password)
    (info "kibot history: " opts " user: " user)
    (make-request (merge
                   {:action "history"
                    :user user
                    :password password}
                   opts))))

(defn splits
  [api-key opts]
  (let [{:keys [user password]} api-key]
    ;(info "login user: " user "pwd: " password)
    (info "kibot history: " opts)
    (make-request (merge
                   {:action "adjustments"
                    :user user
                    :password password}
                   opts))))

(defn snapshot [api-key opts]
  (let [{:keys [user password]} api-key]
    ;(info "login user: " user "pwd: " password)
    (info "kibot snapshot: " opts)
    (make-request (merge
                   {:action "snapshot"
                    :user user
                    :password password}
                   opts))))

; This example will work even if you do not have a subscription:
; http://api.kibot.com/?action=snapshot&symbol=$NDX,AAPL
; return format: Symbol,Date,Time,LastPrice,LastVolume,Open,High,Low,Close,Volume,ChangePercent,TimeZone.

(comment

  (def api-key {:user "guest" :password "guest"})

  (extract-error "asdfasdfasdf")
  (extract-error "405 Data Not Found.\r\nNo data found for the specified period for EURUSD.")

  (extract-error (str "497 Not Allowed.\r\n"
                      "Your account does not have full access to the API.\r\n\r\n"
                      "You can use the 'guest' account for testing and to download "
                      "daily data for stocks and ETFs.\r\n"
                      "For more information, please visit "
                      "http://"
                      "www.kibot.com/api/\r\n\r\n"
                      "Please visit http://www.kibot.com/updates.aspx to see how to subscribe."))

  (extract-error "497 Not Allowed.\r\nYour account does not have full access to the API.\r\n")

  (extract-error "400 Bad Request. Invalid Interval.")

  (login api-key)
  (status)

  (snapshot api-key {:symbol ["$NDX" "AAPL"]})
  (snapshot api-key {:type "future"
                     :symbol "ESZ23"})

  (snapshot api-key {:type "future"
                     :symbol "JYZ23"})

  (snapshot api-key {:symbol ["$NDX"
                              "AAPL"
                              "FCEL"
                              "MSFT"
                              #_"BZ0"]})

  (snapshot api-key {:symbol ["AAPL" "DAX0" "MSFT"]})

  (-> (slurp "../resources/symbollist/futures-kibot.edn")
      (edn/read-string)
      count)
   ;; => 83

  (splits api-key {:symbol "MSFT"})

;  
  )

; url request only (used in symbol-list)
(defn make-request-url [url]
  (let [result (http/get url
                         {:socket-timeout 3000
                          :connection-timeout 3000})
        body (:body result)
        error (extract-error body)]
    ;(info "status:" (:status result))  
    ;(info "headers: " (:headers result))
    (if error
      {:error error}
      body)
    ;  (throw (ex-info (:retMsg result) result))
    )

;  
  )




