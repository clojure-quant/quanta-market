(ns quanta.market.barimport.kibot.asset.db
  (:require
   [clojure.java.io]
   [missionary.core :as m]
   [quanta.market.barimport.kibot.raw :refer [login]]
   [quanta.market.barimport.kibot.http :refer [download-link-info download-link]]
   [quanta.market.barimport.kibot.asset.scraper :refer [calendar-filename calendars]]))

(def prefix "http://api.kibot.com/?action=download&link=")
(def prefix-size (count prefix))

(defn import-calendar [api-key calendar-vec]
  (m/sp
   (let [[exchange f] calendar-vec
         txt  (clojure.java.io/reader (calendar-filename calendar-vec))
         lines (line-seq txt)
         get-info (fn [link-full]
                    (let [link (subs link-full prefix-size)]
                      ;(println "link: " link  )
                      (download-link-info link)))
         tasks (map get-info lines)
         result-fn (fn [& assets]
                     assets)]
     (m/? (login api-key))
     (m/? (apply m/join result-fn tasks)))))







