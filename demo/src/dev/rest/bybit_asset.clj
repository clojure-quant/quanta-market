(ns dev.rest.bybit-asset
  (:require
   [clojure.string :as str]
   [missionary.core :as m]
   [quanta.market.barimport.bybit.raw :as bb]))

(->> (m/? (bb/get-assets "spot"))
     :list
     (map :symbol)
     (filter #(str/starts-with? %  "BTC")))
  ;; => ("BTCUSDT" "BTCUSDC" "BTC3SUSDT" "BTC3LUSDT" "BTCDAI" "BTCBRZ" "BTCEUR" "BTCUSDE" "BTCBRL" "BTCPLN" "BTCTRY")

(->> (m/? (bb/get-assets "linear"))
     :list
     (map :symbol)
     (filter #(str/starts-with? %  "BTC")))
  ;; => ("BTC-01NOV24"
  ;;     "BTC-18OCT24"
  ;;     "BTC-25OCT24"
  ;;     "BTC-26SEP25"
  ;;     "BTC-27DEC24"
  ;;     "BTC-27JUN25"
  ;;     "BTC-28MAR25"
  ;;     "BTC-29NOV24"
  ;;     "BTCPERP"
  ;;     "BTCUSDT")

(->> (m/? (bb/get-assets "inverse"))
     :list
     (map :symbol)
     (filter #(str/starts-with? %  "BTC")))
;; => ("BTCUSD" "BTCUSDH25" "BTCUSDZ24")

;; todo fixme starting from here

(defn get-save [category]
  (->> category
       get-assets
       (spit (str "/home/florian/repo/clojure-quant/quanta-market/resources/bybit-" category ".edn"))))

(get-save "spot")
(get-save "linear")
(get-save "inverse")

(count (get-assets "spot"))    ;; => 596
(count (get-assets "linear"))  ;; => 432
(count (get-assets "inverse")) ;; => 13
(count (get-assets "option"))  ;; => 500