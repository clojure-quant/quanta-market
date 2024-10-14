(ns demo.quote.universe
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.pprint :refer [print-table]]
   [clojure.java.io :as io]))

(defn load-category [category]
  (->
   (str "bybit-" category ".edn")
   (io/resource)
   (slurp)
   (edn/read-string)))

(defn usdt? [{:keys [symbol]}]
  (str/ends-with? symbol "USDT"))

(defn linear-perp? [{:keys [contractType]}]
  (= contractType "LinearPerpetual"))

(def usdt-perpetuals
  (->> (load-category "linear")
       (filter linear-perp?)
       (filter usdt?)))

(def usdt-spot
  (->> (load-category "spot")
       (filter usdt?)))

(count usdt-spot)
;; => 514

(count usdt-perpetuals)
;; => 389

(defn print-perps [perps]
  (->> perps
       (map #(select-keys % [:symbol :status :contractType]))
       (print-table)))

(print-perps usdt-perpetuals)

(defn ->dict [assets]
  (->> assets
       (map (juxt :symbol identity))
       (into {})))

(-> usdt-spot ->dict keys)
(-> usdt-perpetuals ->dict keys)

(def usdt-spot-with-perpetual
  (let [d-perm (-> usdt-perpetuals ->dict)
        spot-perp (map (fn [{:keys [symbol] :as spot}]
                         (if-let [perp (get d-perm symbol)]
                           (assoc spot :perp? true)
                           (assoc spot :perp? false))) usdt-spot)]
    (filter :perp? spot-perp)))

usdt-spot-with-perpetual

(defn print-spot [spots]
  (->> spots
       (map #(select-keys % [:symbol :status ;:contractType
                             ]))
       (print-table)))

(print-spot usdt-spot-with-perpetual)

(def asset-symbols-both
  (map :symbol usdt-spot-with-perpetual))

asset-symbols-both

(count asset-symbols-both)
;; => 209

(pr-str asset-symbols-both)
