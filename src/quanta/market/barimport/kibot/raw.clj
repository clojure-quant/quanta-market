(ns quanta.market.barimport.kibot.raw
  (:require
   [taoensso.telemere :as tm]
   [missionary.core :as m]
   [clj-commons.byte-streams :as bs]
   [quanta.market.util.aleph :as a]))

(defn extract-asset [header]
  (let [m (re-matches #".*filename=(.*)\.txt" header)
        [_ asset] m]
    asset))

(defn get-asset [headers]
  (-> headers
      (get "content-disposition")
      extract-asset))

(defn download-link [url]
  (tm/log! (str "downloading link: " url))
  (m/sp
   (let [response (m/? (a/http-get url {:socket-timeout 90000
                                        :connection-timeout 90000}))
         _ (tm/log! (str "link download finished!"))
         {:keys [headers body]} response
         asset (get-asset headers)]
     {:asset asset
      :data (bs/to-string body)})))







