(ns quanta.market.broker.bybit.topic.orderbook
  (:require
    [missionary.core :as m]))
   

; orderbook responses: type: snapshot,delta

(def test-orderbook-1-updates
  [{:type "snapshot"
    :data {:s "BTCUSDT" :seq 35665538157  :u 56841862}
           :b [[67660.03 0.000189]] :a [[67660.04 0.673526]]}
   {:type "delta"
    :data {:s "BTCUSDT" :seq 35665538162 :u 56841863
           :b [] 
           :a [[67660.04 0.674452]]}}
   {:type "delta"
    :data {:s "BTCUSDT" :seq 35665538174 :u 56841864
           :b [[67660.03 0] [67659.36 0.001968]]
           :a []}}
   {:type "delta"
    :data {:s "BTCUSDT" :seq 35665538188 :u 56841865
           :b [[67659.36 0] [67658.71 0.004138]] 
           :a [[67659.37 0.104626] [67660.04 0]]}}
   {:type "delta"
    :data {:s "BTCUSDT" :seq 35665538219 :u 56841866
           :b []
           :a [[67658.72 0.416688] [67659.37 0]]}}
   {:type "delta"
    :data {:s "BTCUSDT" :seq 35665538287 :u 56841867
           :b []
           :a [[67658.72 0.424]]}}])

; To apply delta updates:
; If you receive an amount that is 0, delete the entry
; If you receive an amount that does not exist, insert it
; If the entry exists, you simply update the value

; b	array	Bids. For snapshot stream, the element is sorted by price in descending order
; b [0]	string	Bid price


(defn create-book [{:keys [a b] :as _snapshot}]
  (let [[b1-price b1-vol] (first b)
        [a1-price a1-vol] (first a)]
    {:bid b1-price
     :bid-vol b1-vol
     :ask a1-price
     :ask-vol a1-vol}))

(defn remove? [[p v]]
  (= v 0))



(defn update-book [book {:keys [a b] :as _delta}]
  (let [a (remove remove? a)
        b (remove remove? b)
        b1 (first b)
        a1 (first a)
        book (if b1 
               (let [[b1-price b1-vol] b1]
                (assoc book :bid b1-price :bid-vol b1-vol) )
               book)
        book (if a1
               (let [[a1-price a1-vol] a1]
                 (assoc book :ask a1-price :ask-vol a1-vol))
               book)]
    book))


(defn process-update [book {:keys [type data]}]
  (case type 
    "snapshot" (create-book data)
    "delta" (update-book book data)))


(comment 
  (seq [])
  (seq [1])
  (remove remove? [[67659.36 0] [67658.71 0.004138]])
  (reduce process-update {} test-orderbook-1-updates)  
 ; 
  )

(defn transform-book-flow [topic-data-flow]
  ; output of this flow:
  (m/reductions process-update {} topic-data-flow)
  )




(def test-orderbook-50-updates
  [{:data {:s "BTCUSDT", :seq 35665795799, :u 101154450
           :b [[67673.65 0.415755] [67672.97 0.000443] [67672.69 0.063884] [67672.53 0.00178] [67672.29 0.000443] [67671.96 0.3915] 
                             [67671.95 0.18] [67671.94 0.00005] [67671.82 0.002739] [67671.67 0.007388] [67671.61 0.000443] 
                             [67670.93 0.000443] [67670.53 0.007349] [67670.25 0.000443] [67669.8 0.007349] [67669.59 0.010704] 
                             [67669.57 0.000443] [67669.52 0.000072] [67668.89 0.000443] [67668.22 0.0506] [67668.21 0.000443] 
                             [67667.56 0.00528] [67667.53 0.000443] [67666.85 0.000443] [67666.54 0.029071] [67666.17 0.000443] 
                             [67665.57 0.479953] [67665.56 0.075309] [67665.55 0.225] [67665.53 0.002736] [67665.49 0.000443] 
                             [67664.81 0.002079] [67664.67 0.029966] [67664.5 0.034676] [67664.32 0.029558] [67664.25 0.000048] 
                             [67664.13 0.000443] [67663.45 0.000443] [67662.77 0.000443] [67662.75 0.030072] [67662.09 0.000443] 
                             [67661.91 0.065255] [67661.9 0.03] [67661.56 0.038515] [67661.41 0.000443] [67661.4 0.03] [67660.73 0.000443] 
                             [67660.7 0.029559] [67660.29 0.002684] [67660.26 0.007388]],
           :a [[67673.66 1.114781] [67673.67 0.022126] [67673.68 0.007349] [67674.25 0.003069] [67674.26 0.032413] [67674.33 0.002411] 
               [67675.01 0.000443] [67675.69 0.002459] [67676.37 0.000443] [67676.39 0.463284] [67676.41 0.036089] [67677.05 0.000443] 
               [67677.37 0.213] [67677.49 0.007388] [67677.72 0.00936] [67677.73 0.000443] [67678.41 0.004607] [67678.6 0.010943] 
               [67679.09 0.000443] [67679.1 0.024422] [67679.75 0.00528] [67679.77 0.000443] [67679.78 0.024] [67680.41 0.024] 
               [67680.45 0.000443] [67681.13 0.000443] [67681.75 0.000048] [67681.78 0.002736] [67681.81 0.000443] [67681.82 0.024] 
               [67682.17 0.029886] [67682.2 0.036443] [67682.49 0.677534] [67682.86 0.024] [67683.07 0.000072] [67683.17 0.000443] 
               [67683.53 0.234805] [67683.85 0.002443] [67684.48 0.002217] [67684.53 0.000443] [67685.07 0.010001] [67685.21 0.000443] 
               [67685.22 0.024052] [67685.24 0.039403] [67685.89 0.000443] [67685.91 0.206021] [67686.57 0.000443] [67686.75 0.000048] 
               [67687.07 0.094722] [67687.12 0.003247]]}, :type "snapshot"}
{:data {:s "BTCUSDT", :seq 35665795813 :u 101154451
        :b [[67671.96 0] [67668.23 0.110057]]
        :a [[67673.66 1.117132] [67675.79 0.063884] [67687.12 0]]}, :type "delta"}
{:data {:s "BTCUSDT", :seq 35665795834, :u 101154452
        :b [] 
        :a [[67678.41 0.003619]] }, :type "delta"}
{:data {:s "BTCUSDT", :seq 35665795843 :u 101154453
        :b [[67673.65 0.410794] [67671.67 0.014759]], , 
        :a [] }, :type "delta"}
{:data {:s "BTCUSDT", :b [[67673.65 0.413794]], :seq 35665795851, :a [[67675.8 0.000083] [67687.07 0]], :u 101154454}, :type "delta"}
{:data {:s "BTCUSDT", :b [[67673.65 0.269734]], :seq 35665795864, :a [[67674.12 0.03] [67678.41 0.000443] [67686.75 0]], :u 101154455}, :type "delta"}
{:data {:s "BTCUSDT", :b [[67672.7 0.391503] [67668.23 0]], :seq 35665795876, :a [], :u 101154456}, :type "delta"}])

;(def snapshot-data (-> test-orderbook-50-updates first :data))

;snapshot-data