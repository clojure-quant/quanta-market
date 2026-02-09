(ns quanta.market.barimport.kibot.http
  (:require
   [taoensso.timbre :refer [info warn error]]
   [taoensso.telemere :as tm]
   [missionary.core :as m]
   [clojure.java.io :as io]
   [tick.core :as t]
   [tech.v3.dataset :as tds]
   [tablecloth.api :as tc]
   [tech.v3.datatype :as dtype]
   [clj-commons.byte-streams :as bs]
   [ta.calendar.calendars :refer [calendars]]
   [quanta.market.asset.db :as db]
   [quanta.bar.protocol :refer [barsource]]
   [quanta.market.barimport.kibot.helper :refer [adjust-time-to-exchange-close]]
   [quanta.market.barimport.kibot.raw :as kibot]
   [quanta.market.util.aleph :as a]
   [quanta.market.util.clj-http :refer [http-head]]))

;; LINK INFO

(defn extract-asset [header]
  (let [m (re-matches #".*filename=(.*)\.txt" header)
        [_ asset] m]
    asset))

(defn download-link-info [link]
  (let [; opts need to be defined outside the sp. this is because inside the qps get reordered.
        opts {:query-params {:action "download" ; action needs to be first, otherwise kibot api fails.
                             :link link}}]
    (m/sp
     (let [base-url "http://api.kibot.com/"
           response (m/? (http-head base-url opts))
           {:keys [headers body]} response
           ;_ (println " headers: " (keys headers))
           cd (get headers "content-disposition")]
       (if cd
         (let [result {:kibot-asset (extract-asset cd)
                       :link link}]
           (tm/log! (str "kibot asset: " (:kibot-asset result)))
           result)
         (do (tm/log! (str "missing content-disposition-header body: \r\n" body))
             #_(throw (ex-info "missing-content-disposition-header"
                               {:url link
                                :body body}))
             {:error "missing-content-disposition-header"}))))))

(def prefix "http://api.kibot.com/?action=download&link=")
(def prefix-size (count prefix))

(defn remove-prefix [link-with-url]
  (subs link-with-url prefix-size))

;; download csv file

(defn download-link [link]
  ; download request does not support login!
  (kibot/make-request {:action "download" :link link}))

;; kibot intraday times are in EST and bar open time.

(defn date-time->zoned
  "returns the date as instant set to the bar close in EST"
  [dt time]
  (-> (t/at dt time)
      (t/in "America/New_York")
      ;(to-bar-close 1 :minutes)   ; kibot can have more bars than the quanta calendar expects. so no alignment to quanta calendar day close here.
      (t/instant)))

(defn date-time-adjust [bar-ds]
  (let [date-vec (:date bar-ds)
        time-vec (:time bar-ds)
        date-time-vec  (dtype/emap date-time->zoned
                                   :instant
                                   date-vec time-vec)]
    (tc/add-or-replace-column bar-ds :date date-time-vec)))

(defn string->stream [s]
  (io/input-stream (.getBytes s "UTF-8")))

(defn kibot-result-m->dataset [csv]
  (-> (tds/->dataset (string->stream csv)
                     {:file-type :csv
                      :header-row? false
                      :dataset-name "kibot-bars"})
        ; 05/22/2023,01:27,0.409523291,0.409582704,0.409314088,0.409314088,4
      (tc/rename-columns {"column-0" :date
                          "column-1" :time
                          "column-2" :open
                          "column-3" :high
                          "column-4" :low
                          "column-5" :close
                          "column-6" :volume})
        ;(tc/convert-types :date [[:local-date-time date->localdate]])
      date-time-adjust
      (tc/drop-columns [:time])))

;; DAY

(defn kibot-result-d->dataset [csv exchange]
  (-> (tds/->dataset (string->stream csv)
                     {:file-type :csv
                      :header-row? false
                      :dataset-name "kibot-bars"})
        ; 12/28/2009,0.30652,0.30694,0.30612,0.30694,19
      (tc/rename-columns {"column-0" :date
                          "column-1" :open
                          "column-2" :high
                          "column-3" :low
                          "column-4" :close
                          "column-5" :volume})
      (adjust-time-to-exchange-close exchange)))

(defrecord import-kibot-http [api-key]
  barsource
  (get-bars [this opts window]
    ; no need to call window->open-time here
    (m/sp
     (let [{:keys [asset calendar]} opts
           [exchange f] calendar
           asset (db/instrument-details asset)
           kibot-link (case f
                        :d (:kibot-link-d asset)
                        :m (:kibot-link-m asset))]
       (if kibot-link
         (let [_ (m/? (kibot/login api-key))  ; http downlaods needs login first.
               csv  (m/? (download-link kibot-link))]
           (case f
             :m (kibot-result-m->dataset csv)
             :d (kibot-result-d->dataset csv exchange)))
         (throw (ex-info "kibot-http link not in asset-db" {:asset asset
                                                            :f f})))))))

(defn create-import-kibot-http [api-key]
  (import-kibot-http. api-key))
