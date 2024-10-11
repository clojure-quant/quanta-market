(ns quanta.market.robot.position.working
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.robot.position.roundtrip :refer [return-prct return-abs return-log]])
  (:import [missionary Cancelled]))

(defn- value-position [position
                       {:keys [time price] :as quote}]
  ;(println "value-position: " position " quote: " quote)
  ; {:asset BTCUSDT, :feed :bybit, :qty 500, 
  ;  :entry-price 1000.0, :entry-date #time/instant "2024-08-12T20:34:29.509589236Z"}  
  ; {:asset BTCUSDT, :price 59134.96, :size 0.001654, :time 1723494873630, :BT false, 
  ;  :S Sell, :i 2290000000311285085}
  (let [roundtrip (assoc position
                         :exit-date (t/instant 1723494873630)
                         :exit-price price)
        abs-ret (return-abs roundtrip)]
    (if quote
      (assoc roundtrip
             :ret-abs abs-ret
             :ret-prct (return-prct roundtrip)
             :ret-log (return-log roundtrip)
             :win? (> abs-ret 0.0))
      roundtrip)))

(defn working-position [qm position]
  (m/ap
   (let [sub (select-keys position [:asset :feed])
         _ (println "start wp: " position " sub: " sub)
         quote (p/trade qm sub)
         current-quote (m/?> quote)]
     (when current-quote
       (value-position position current-quote)))))





