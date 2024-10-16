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

