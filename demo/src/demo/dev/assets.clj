(ns demo.dev.assets
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.pprint :refer [print-table]]
   [clojure.java.io :as io]
   ))


(defn load-category [category]
  (->
   (str "bybit-" category ".edn")
   (io/resource)
   (slurp)
   (edn/read-string)))



(defn usdt? [{:keys [symbol]}]
  (str/ends-with? symbol "USDT"))

(defn usdc? [{:keys [symbol]}]
  (str/ends-with? symbol "USDC"))

(defn usde? [{:keys [symbol]}]
  (str/ends-with? symbol "USDE"))

(defn btc? [{:keys [symbol]}]
  (str/ends-with? symbol "BTC"))

(defn eur? [{:keys [symbol]}]
  (str/ends-with? symbol "EUR"))

(defn sdc? [{:keys [symbol]}]
  (str/ends-with? symbol "SDC"))

(->> (load-category "spot")
     (map #(select-keys % [:symbol :status]))
     (filter sdc?)
     ;(remove usdt?)
     ;(remove usdc?)
     ;(remove usde?)
     ;(remove btc?)

     count
     ;(print-table)
     ;first
)

USDT pairs:  514
USDC pairs    37
Non USDT pairs: 84 
  BTC 12
  USDC
  DAI
  SDC 37
  EUR 21
  USDE 3
  BRL

  

;  USDT perpetual, and USDC contract, including USDC perp, USDC futures

(defn perp? [{:keys [symbol]}]
  (str/ends-with? symbol "PERP"))

(defn linear-perp? [{:keys [contractType]}]
  (= contractType "LinearPerpetual"))


(->> (load-category "linear")
     (filter linear-perp?)
     (filter usdt?)
     (map #(select-keys % [:symbol :status :contractType]))
     ;(filter sdc?)
     ;( usdt?)
     ;(filter perp?)
     
     ;(remove usde?)
     ;(remove btc?)
     ;(filter #(= "XEMUSDT" (:symbol %)))
     ;(filter #(= "LinearFutures" (:contractType %)))
     ;count
     (print-table)
     ;first
     )

  
:contractType "LinearPerpetual"
  ; linear 432
  ; usdt 389
  ; perp: 23
  ; usdt 


|      :symbol | :status |
|--------------+---------|
| 1000PEPEPERP | Trading |
|     AEVOPERP | Trading |
|      ARBPERP | Trading |
|      BNBPERP | Trading |
|      BTCPERP | Trading |
|     DOGEPERP | Trading |
|      ETCPERP | Trading |
|    ETHFIPERP | Trading |
|      ETHPERP | Trading |
|    MATICPERP | Trading |
|      MNTPERP | Trading |
|     ONDOPERP | Trading |
|       OPPERP | Trading |
|     ORDIPERP | Trading |
| SHIB1000PERP | Trading |
|      SOLPERP | Trading |
|     STRKPERP | Trading |
|      SUIPERP | Trading |
|      TIAPERP | Trading |
|      TONPERP | Trading |
|      WIFPERP | Trading |
|      WLDPERP | Trading |
|      XRPPERP | Trading |


(->> (load-category "spot")




