(ns ta.import.provider.kibot-http.raw
  (:require
   [taoensso.timbre :refer [info warn error]]
   [clojure.string :as str]
   [aleph.http :as http]
   [clj-commons.byte-streams :as bs]))

(defn extract-asset [header]
  (let [m (re-matches #".*filename=(.*)\.txt" header)
        [_ asset] m]
    asset))

(defn get-asset [request]
  (-> request
      :headers
      (get "content-disposition")
      extract-asset))

(defn download-link [url]
  (info "downloading link: " url)
  (let [request @(http/get url {:socket-timeout 90000
                                :connection-timeout 90000})
        asset (get-asset request)]
    {:asset asset
     :data (bs/to-string (:body request))}))

(defn download-link-csv [url]
  (info "downloading link: " url)
  (let [request @(http/get url)]
    (bs/to-string (:body request))))

(defn save-csv-data [asset data]
  (spit (str "output/kibot-http/" asset ".txt")
        data))

(defn load-csv-data [asset]
  (slurp (str "output/kibot-http/" asset ".txt")))

(defn download-import [url]
  (println "downloading url: " url)
  (let [{:keys [asset data]} (download-link url)]
    (save-csv-data asset data)))





