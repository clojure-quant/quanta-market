(ns demo.universe-vol
  (:require
   [clojure.pprint :refer [print-table]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :refer [current-v]]
   [quanta.market.broker.bybit.quotefeed] ; side effects
   [demo.logging] ; for side effects
   [demo.universe :refer [asset-symbols-both]]))

(def bb-quote (p/create-quotefeed {:type :bybit}))



(defn stats-t [asset]
  (current-v
   (p/get-topic bb-quote {:topic :asset/stats
                          :asset asset})))




(m/? (stats-t "BTCUSDT"))
;; => {:open 68027.25,
;;     :index 69763.215027,
;;     :value 9.550210782792867E8,
;;     :close 69768.16,
;;     :volume 13862.049667,
;;     :high 69954.44,
;;     :low 67585.42,
;;     :change 0.0256,
;;     :asset "BTCUSDT"}

; montag 8:00
;; => {:open 68017.73,
;;     :index nil,
;;     :value 9.534477949602457E8,
;;     :close 69711.05,
;;     :volume 13840.624695,
;;     :high 69954.44,
;;     :low 67585.42,
;;     :change 0.0249,
;;     :asset "BTCUSDT"}

; sonntag 14:00
;; => {:open 68053.37,
;;     :index 68115.665287,
;;     :value 1.2143808019240878E9,   
;;     :close 68123.6,
;;     :volume 17897.920765,
;;     :high 69209.26,
;;     :low 66635.03,
;;     :change 0.001,
;;     :asset "BTCUSDT"}

(m/? (stats-t "BTCUSDT.L"))
;; => Execution error (IllegalArgumentException) at quanta.market.broker.bybit.topic.stats/normalize-bybit-stats (REPL:28).
;;    Expected string, got nil
;; does not work for linear
;; linear gives back this: {:ask1Size 0.832, :symbol BTCUSDT, :ask1Price 69766.10}



(defn volume-t [asset]
  (let [stats (stats-t asset)]
    (m/sp (let [s (m/? stats)]
       {:asset asset
        :value (:value s)
        :close (:close s)}))))

(m/? (volume-t "BTCUSDT"))
;; => {:asset "BTCUSDT", :value 9.548235889833379E8, :close 69756.01}
;; => {:asset "BTCUSDT", :close 68007.54, :value 1.087035021867885E9}

(defn wrap-blk [t]
  (m/via m/blk (m/? t)))


(defn overview-t [assets]
  (let [tasks (map volume-t assets)
        tasks (map wrap-blk tasks)]
    (apply m/join vector tasks)))

(m/? (overview-t ["BTCUSDT" "ETHUSDT" ]))
;; => [{:value 9.55591496414747E8, :asset "BTCUSDT", :close 69884.27}
;;     {:value 3.12556045574484E8, :asset "ETHUSDT", :close 3378.26}]

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
;; => nil


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

; monday 8am
;|              :value |   :asset |   :close |
;|---------------------+----------+----------|
;| 9.558369120861392E8 |  BTCUSDT | 69846.38 |
;| 3.127008261253744E8 |  ETHUSDT |  3376.19 |
;|   2.2191121393795E8 |  SOLUSDT |   192.88 |
;|   8.1623782329512E7 |  MNTUSDT |   0.8114 |
;|    7.689392925359E7 | USDCUSDT |   1.0001 |
;|   5.3195338691367E7 |  XRPUSDT |   0.6102 |
;|   2.3211122106648E7 | DOGEUSDT |  0.13384 |
;|  2.15595684564817E7 | ONDOUSDT |  1.01321 |
;|   2.1079925735354E7 |  TONUSDT |   6.7539 |
;|   1.7429997814938E7 |  WIFUSDT |   2.5353 |



