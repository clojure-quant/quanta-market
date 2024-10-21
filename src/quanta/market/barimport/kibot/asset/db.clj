(ns quanta.market.barimport.kibot.asset.db
  (:require
   [clojure.java.io]
   [missionary.core :as m]
   [taoensso.telemere :as tm]
   [quanta.market.barimport.kibot.raw :refer [login]]
   [quanta.market.barimport.kibot.http :refer [download-link-info remove-prefix]]
   [quanta.market.barimport.kibot.asset.scraper :refer [calendar-filename calendars]]))

(defonce asset-db (atom {}))

(defn has-asset [link]
  (get @asset-db link))

(defn download-link-and-add [calendar-vec link]
  (m/sp
   (let [{:keys [kibot-asset] :as data} (m/? (download-link-info link))]
     (when kibot-asset
       (swap! asset-db assoc
              link
              (assoc data :calendar calendar-vec))))))

(defn dump-db []
  (spit ".data/kibot-db.edn" (pr-str (vals @asset-db))))

(defn- limit-task [sem blocking-task]
  (m/sp
   (m/holding sem (m/? blocking-task))))

(defn import-calendar [api-key parallel-nr calendar-vec]
  (m/sp
   (let [[exchange f] calendar-vec
         txt  (clojure.java.io/reader (calendar-filename calendar-vec))
         lines (line-seq txt)
         ;tasks (map download-link-info2 lines)
         lines (map remove-prefix lines)
         lines (remove has-asset lines)
         _ (tm/log! (str "downloading items: "  (count lines)))
         tasks (map (partial download-link-and-add calendar-vec) lines)
         sem (m/sem parallel-nr)
         tasks-limited (map #(limit-task sem %) tasks)
         result-fn (fn [& assets]
                     assets)]
     (m/? (login api-key))
     (m/? (apply m/join result-fn tasks-limited)))
   :import-calendar-finished))

(defn normalize-asset [{:keys [link kibot-asset calendar]}]
  ; {:link "", :kibot-asset "TTDGBP", :calendar [:forex :m]}
  (let [[category f] calendar
        link-key (case f
                   :m :kibot-link-m
                   :d :kibot-link-d)]
    (assoc {:kibot-asset kibot-asset
            :kibot-category category}
           link-key link)))

(defn add-asset [assets asset]
  (let [a (if-let [existing-asset (get assets :kibot-asset)]
            (merge existing-asset asset)
            asset)]
    (assoc assets (:kibot-asset a) a)))

(defn normalized-db []
  (let [assets (vals @asset-db)
        normalized-assets (map normalize-asset assets)
        assets (reduce add-asset {} normalized-assets)]
    assets))

(defn write-normalized-db []
  (let [db (normalized-db)]
    (spit ".data/kibot.edn" (pr-str db))))




