(ns dev.asset.raw.kibot
  (:require
   [missionary.core :as m]
   [quanta.market.barimport.kibot.asset.db :as db]))

(defn transform-calendar [calendar]
  (->> @db/asset-db
       vals
     ;first
       (filter #(= (:calendar %) calendar))
       (map (fn [{:keys [kibot-asset link calendar]}]
              (let [[exchange f] calendar
                    key (case f
                          :d :kibot-link-d
                          :m :kibot-link-m)]
                (assoc
                 {:kibot kibot-asset}
                 key link))))))

(defn transform-category [c]
  (let [day (transform-calendar [c :d])
        min (transform-calendar [c :m])
        dict-day (->> day
                      (map (juxt :kibot identity))
                      (into {}))
        dict-all (reduce (fn [dict {:keys [kibot] :as row}]
                           (if-let [existing (get dict kibot)]
                             (assoc dict kibot (merge existing row))
                             (assoc dict kibot row))) dict-day min)
        assets (vals dict-all)]
    (spit (str "../resources/asset/raw/kibot-" (name c) ".edn") (pr-str assets))
    (count assets)))

;; test transform calendar

(-> (transform-calendar [:etf :d])
    first)
;; => Syntax error compiling at (src/dev/asset/raw/kibot.clj:36:5).
;;    Unable to resolve symbol: transform-calendar in this context

(transform-calendar [:etf :m])

;; transform data

(transform-category :etf)
;; => 5570

(transform-category :forex)
;; => 1078

(transform-category :futures)
;; => 8548



