(ns demo.universe-vol
  (:require
   [clojure.pprint :refer [print-table]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [start-flow-logger! stop! start-flow-printer!
                               current-v]]
   [quanta.market.broker.bybit.quotefeed] ; side effects
   [demo.logging] ; for side effects
   [demo.universe :refer [asset-symbols-both]]))

(def bb-quote (p/create-quotefeed {:type :bybit}))


(defn stats-t [asset]
  (current-v
   (p/get-topic bb-quote {:topic :asset/stats
                          :asset asset})))

(m/? (stats-t "BTCUSDT"))
;; => {:open 68053.37,
;;     :index 68115.665287,
;;     :value 1.2143808019240878E9,
;;     :close 68123.6,
;;     :volume 17897.920765,
;;     :high 69209.26,
;;     :low 66635.03,
;;     :change 0.001,
;;     :asset "BTCUSDT"}

(defn volume-t [asset]
  (let [stats (stats-t asset)]
    (m/sp (let [s (m/? stats)]
       {:asset asset
        :value (:value s)
        :close (:close s)}))))

(m/? (volume-t "BTCUSDT"))
;; => {:asset "BTCUSDT", :close 68007.54, :value 1.087035021867885E9}

(defn wrap-blk [t]
  (m/via m/blk (m/? t)))


(defn overview-t [assets]
  (let [tasks (map volume-t assets)
        tasks (map wrap-blk tasks)
        ]
    (apply m/join vector tasks)))

(m/? (overview-t ["BTCUSDT" "ETHUSDT"]))
;; => [{:asset "BTCUSDT", :close 67998.0, :value 1.0872855296622603E9}
;;     {:asset "ETHUSDT", :close 3264.67, :value 3.118502665919389E8}]

; account size: 10k. 10x leverage = 100k trade.
; a trade can be over 1 hour.
; therefore every hour we need at least 100k volume.
; -> min volume per day: 100k * 24 = 2.4 million.
; this is the absolute minimum. because it means a fill takes one hour.
; at fill 6 min: 2.4 * 10 = 24 million.
; 25 million is our BARE MINIMUM for HOURLY STRATEGY.

(def overview (m/? (overview-t asset-symbols-both)))

overview

(print-table (reverse (sort-by :value overview)))

(def our-universe 
  (filter #(> (:value %) 15000000.0) overview))


(print-table (reverse (sort-by :value our-universe)))

; |   :close |              :value |   :asset |
; |----------+---------------------+----------|
; | 68089.54 | 1.128579664712057E9 |  BTCUSDT |
; |  3269.15 | 3.248433155842764E8 |  ETHUSDT | x3 weniger
; |    184.8 |   1.9700455063675E8 |  SOLUSDT |
; |   0.7972 |   8.8902977617348E7 |  MNTUSDT |
; |   1.0001 |   7.9542247134599E7 | USDCUSDT | xx
; |   0.6015 |   5.5843527554992E7 |  XRPUSDT |
; |  0.12972 |   2.4783186254902E7 | DOGEUSDT |
; |    6.616 |   1.9114847919191E7 |  TONUSDT |
; |   2.4226 |   1.6734880202166E7 |  WIFUSDT |






