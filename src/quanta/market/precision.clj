(ns quanta.market.precision)


;; => ({:lotSizeFilter
;;      {:basePrecision "0.000001",
;;       :quotePrecision "0.00000001",
;;       :minOrderQty "0.000048",
;;       :maxOrderQty "71.73956243",
;;       :minOrderAmt "1",
;;       :maxOrderAmt "4000000"},
;;      :innovation "0",
;;      :symbol "BTCUSDT",
;;      :quoteCoin "USDT",
;;      :priceFilter {:tickSize "0.01"},
;;      :baseCoin "BTC",
;;      :marginTrading "both",
;;      :status "Trading",
;;      :riskParameters {:limitParameter "0.03", :marketParameter "0.03"}})


;; The "M" suffix denotes a BigDecimal instance
;; http://download.oracle.com/javase/6/docs/api/java/math/BigDecimal.html

 ;(with-precision 5 :rounding HalfUp
 ;               (+ 1000000000M 1E-20M)) 

(defn round2
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))


(def assets 
  [{:asset "BTCUSDT" :precision 2}
   {:asset "ETHUSDT" :precision 3}
   ])

(def asset-dict 
  (->> (map (juxt :asset identity) assets)
       (into {})))
   

(defn round-asset [asset price]
  (let [{:keys [precision]} (get asset-dict asset)]
    (round2 precision price)))



(comment
    (round-asset "ETHUSDT" 6000.123456789123456789M )
    (round-asset "BTCUSDT" 60000.123456789123456789M)
    ;; => 60000.123457
    ;;         "0.000001"

(-> (with-precision 10 (+ 60000.123456789123456789M 0.0M))
    str)  

   (def m (with-precision 5 (/ 2M 3M)))
   m
  (str m)

  (-> (round2 2 50000.12345689M)
      type
   )
  
  
  (map (partial round2 2) [0.001 10.123456 9.5556])
 ; => 0.67M  
  
  
  ;
  )


