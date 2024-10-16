(ns dev.rest.bybit-history
  (:require
   [clojure.string :as str]
   [tick.core :as t]
   [missionary.core :as m]
   [quanta.market.barimport.bybit.raw :as bb]
   [quanta.market.barimport.bybit.normalize-request :refer [bybit-bar-params]]))

(def start-date-daily (t/instant "2018-11-01T00:00:00Z"))

(t/instant 1669852800000)
(t/instant 1693180800000)
(t/instant 1709673240000)

(-> (t/instant) type)
    ;; => java.time.Instant
(-> (t/inst) type)
    ;; => java.util.Date    WE DO NOT WANT THIS ONE!

(m/? (bb/get-bars
      {:symbol "BTCUSD"
       :start 1669852800000
       :interval "D"
       :category "inverse"
       :limit 3}))
  ;; => ({:date #time/instant "2022-12-03T00:00:00Z",
  ;;      :open 17090.5,
  ;;      :high 17148.0,
  ;;      :low 16844.0,
  ;;      :close 16884.0,
  ;;      :volume 1.60202594E8,
  ;;      :turnover 9439.93499735}
  ;;     {:date #time/instant "2022-12-02T00:00:00Z",
  ;;      :open 16965.5,
  ;;      :high 17092.5,
  ;;      :low 16749.0,
  ;;      :close 17090.5,
  ;;      :volume 3.13767168E8,
  ;;      :turnover 18524.3541774}
  ;;     {:date #time/instant "2022-12-01T00:00:00Z",
  ;;      :open 17148.0,
  ;;      :high 17334.0,
  ;;      :low 16846.0,
  ;;      :close 16965.5,
  ;;      :volume 3.54699342E8,
  ;;      :turnover 20785.98728504})

(m/? (bb/get-bars
      {:symbol "BTCUSDT"
       :start (-> "2024-03-05T00:00:00Z" t/instant t/long (* 1000))
       :end (-> "2024-03-06T00:05:00Z" t/instant t/long (* 1000))
       :interval "1"
       :category "spot"   ; default linear
       :limit 3}))
 ;; => ({:date #time/instant "2024-03-06T00:05:00Z",
 ;;      :open 63594.55,
 ;;      :high 63626.04,
 ;;      :low 63510.55,
 ;;      :close 63583.52,
 ;;      :volume 37.853568,
 ;;      :turnover 2406083.53048051}
 ;;     {:date #time/instant "2024-03-06T00:04:00Z",
 ;;      :open 63652.01,
 ;;      :high 63694.01,
 ;;      :low 63557.71,
 ;;      :close 63594.55,
 ;;      :volume 70.183865,
 ;;      :turnover 4465759.37566942}
 ;;     {:date #time/instant "2024-03-06T00:03:00Z",
 ;;      :open 63756.72,
 ;;      :high 63756.72,
 ;;      :low 63625.59,
 ;;      :close 63652.01,
 ;;      :volume 37.997742,
 ;;      :turnover 2419313.192522})

; first row is the LAST date.
; last row is the FIRST date
; if result is more than limit, then it will return LAST values first.

(m/? (bb/get-bars-ds
      {:symbol "BTCUSDT"
       :start (-> "2024-03-05T00:00:00Z" t/instant t/long (* 1000))
       :end (-> "2024-03-06T00:05:00Z" t/instant t/long (* 1000))
       :interval "1"
       :category "spot"   ; default linear
       :limit 3}))
;; => _unnamed [3 6]:
;;    
;;    |                :date |    :open |    :high |     :low |   :close |   :volume |
;;    |----------------------|---------:|---------:|---------:|---------:|----------:|
;;    | 2024-03-06T00:03:00Z | 63756.72 | 63756.72 | 63625.59 | 63652.01 | 37.997742 |
;;    | 2024-03-06T00:04:00Z | 63652.01 | 63694.01 | 63557.71 | 63594.55 | 70.183865 |
;;    | 2024-03-06T00:05:00Z | 63594.55 | 63626.04 | 63510.55 | 63583.52 | 37.853568 |

(bybit-bar-params
 {:asset "BTCUSDT"
  :calendar [:crypto :m]}
 {:start (-> "2024-03-05T00:00:00Z" t/instant)
  :end (-> "2024-03-06T00:00:00Z" t/instant)})
;; => {:symbol "BTCUSDT", :interval "1",  :category "spot", 
;;     :start 1709596800000, :end 1709683200000}

(m/? (bb/get-bars-ds-normalized
      {:asset "BTCUSDT"
       :calendar [:crypto :m]}
      {:start (-> "2024-03-05T00:00:00Z" t/instant)
       :end (-> "2024-03-06T00:00:00Z" t/instant)}))
;; => _unnamed [200 6]:
;;    
;;    |                :date |    :open |    :high |     :low |   :close |    :volume |
;;    |----------------------|---------:|---------:|---------:|---------:|-----------:|
;;    | 2024-03-05T20:41:00Z | 61623.76 | 61772.58 | 61485.41 | 61595.79 | 132.771882 |
;;    | 2024-03-05T20:42:00Z | 61595.79 | 62091.75 | 61547.45 | 62091.75 |  72.862768 |
;;    | 2024-03-05T20:43:00Z | 62091.75 | 62237.77 | 62009.60 | 62223.15 |  35.641349 |
;;    | 2024-03-05T20:44:00Z | 62223.15 | 62230.38 | 62069.47 | 62083.19 |  19.046807 |
;;    | 2024-03-05T20:45:00Z | 62083.19 | 62206.67 | 62055.87 | 62206.66 |  13.149395 |
;;    | 2024-03-05T20:46:00Z | 62206.66 | 62554.94 | 62201.19 | 62531.50 |  24.885035 |
;;    | 2024-03-05T20:47:00Z | 62531.50 | 62610.66 | 62427.29 | 62431.24 |  20.521212 |
;;    | 2024-03-05T20:48:00Z | 62431.24 | 62431.24 | 62080.00 | 62080.01 |  35.890440 |
;;    | 2024-03-05T20:49:00Z | 62080.01 | 62234.48 | 62028.65 | 62128.02 |  34.993207 |
;;    | 2024-03-05T20:50:00Z | 62128.02 | 62196.34 | 62028.83 | 62148.87 |  40.165484 |
;;    |                  ... |      ... |      ... |      ... |      ... |        ... |
;;    | 2024-03-05T23:50:00Z | 64295.89 | 64330.00 | 64209.51 | 64297.50 |  43.923542 |
;;    | 2024-03-05T23:51:00Z | 64297.50 | 64337.99 | 64238.00 | 64337.99 |  41.079387 |
;;    | 2024-03-05T23:52:00Z | 64337.99 | 64345.44 | 64273.23 | 64273.23 |  31.329395 |
;;    | 2024-03-05T23:53:00Z | 64273.23 | 64288.49 | 64105.36 | 64106.02 |  49.589248 |
;;    | 2024-03-05T23:54:00Z | 64106.02 | 64115.99 | 63876.12 | 63951.75 |  92.306359 |
;;    | 2024-03-05T23:55:00Z | 63951.75 | 63965.99 | 63746.24 | 63816.73 |  76.627406 |
;;    | 2024-03-05T23:56:00Z | 63816.73 | 63846.72 | 63725.33 | 63819.99 |  59.380323 |
;;    | 2024-03-05T23:57:00Z | 63819.99 | 63841.70 | 63732.00 | 63732.00 |  30.331934 |
;;    | 2024-03-05T23:58:00Z | 63732.00 | 63754.96 | 63661.77 | 63697.99 |  32.260888 |
;;    | 2024-03-05T23:59:00Z | 63697.99 | 63814.44 | 63680.00 | 63723.09 |  31.596116 |
;;    | 2024-03-06T00:00:00Z | 63723.09 | 63812.45 | 63694.32 | 63811.99 |  44.278701 |


