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
   [quanta.market.util.aleph :as a]
   [ta.db.asset.db :as db]
   [ta.db.bars.protocol :refer [barsource]]
   [quanta.market.barimport.kibot.raw :as kibot]))

;; LINK INFO

(defn extract-asset [header]
  (let [m (re-matches #".*filename=(.*)\.txt" header)
        [_ asset] m]
    asset))

(defn download-link-info [link]
  (let [qp {:action "download"
            :link link}]
    (m/sp
     (let [response (m/? (kibot/make-request-raw qp))
           {:keys [headers body]} response
           _ (println " headers: " (keys headers))
           cd (get headers "content-disposition")]
       (if cd
         {:kibot-asset (extract-asset cd)
          :link link}
         (throw (ex-info "missing-content-disposition-header"
                         {:url link
                          :body (bs/to-string body)})))))))

(def prefix "http://api.kibot.com/?action=download&link=")
(def prefix-size (count prefix))

(defn remove-prefix [link-with-url]
  (subs link-with-url prefix-size))

(defn download-link-info2 [link-with-url]
  (m/sp
   (let [link (remove-prefix link-with-url)
         r (m/? (download-link-info link))]
     (tm/log! (str "kibot asset: " r))
     r)))

;; download csv file

(defn download-link [link]
  ; download request does not support login!
  (kibot/make-request {:action "download" :link link}))

;; kibot intraday times are in EST.

(defn date-time->zoned [dt time]
  (-> (t/at dt time)
      (t/in "America/New_York")))

(defn date-time-adjust [bar-ds]
  (let [date-vec (:date bar-ds)
        time-vec (:time bar-ds)
        date-time-vec  (dtype/emap date-time->zoned
                                   :zoned-date-time
                                   date-vec time-vec)]
    (tc/add-or-replace-column bar-ds :date date-time-vec)))

(defn string->stream [s]
  (io/input-stream (.getBytes s "UTF-8")))

(defn kibot-result->dataset [csv]
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

(defrecord import-kibot-http [api-key]
  barsource
  (get-bars [this opts window]
    (m/sp
     (let [{:keys [asset calendar]} opts
           {:keys [kibot-http]} (db/instrument-details asset)]
       (if kibot-http
         (let [; (login api-key) ; not sure if http needs login first.
               csv  (m/? (download-link kibot-http))]
           (kibot-result->dataset csv))
         (throw (ex-info "kibot-http-url not in asset-db" {:asset asset})))))))

(defn create-import-kibot-http [api-key]
  (import-kibot-http. api-key))