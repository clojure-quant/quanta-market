(ns quanta.market.barimport.kibot.asset.db
  (:require
   [clojure.java.io]
   [missionary.core :as m]
   [quanta.market.barimport.kibot.raw :refer [login]]
   [quanta.market.barimport.kibot.http :refer [download-link-info2]]
   [quanta.market.barimport.kibot.asset.scraper :refer [calendar-filename calendars]]))

(def prefix "http://api.kibot.com/?action=download&link=")
(def prefix-size (count prefix))

(defn- limit-task [sem blocking-task]
  (m/sp
   (m/holding sem (m/? blocking-task))))


(defn import-calendar [api-key parallel-nr calendar-vec]
  (m/sp
   (let [[exchange f] calendar-vec
         txt  (clojure.java.io/reader (calendar-filename calendar-vec))
         lines (line-seq txt)
         tasks (map download-link-info2 lines)
         sem (m/sem parallel-nr)
         tasks-limited (map #(limit-task sem %) tasks)
         result-fn (fn [& assets]
                     assets)]
     (m/? (login api-key))
     (m/? (apply m/join result-fn tasks-limited)))))








