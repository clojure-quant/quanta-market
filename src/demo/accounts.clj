(ns demo.accounts)


(defn get-creds [account-id]
  (-> (System/getenv "MYVAULT")
      (str "/goldly/quanta.edn")
      slurp
      read-string
      account-id))

(def accounts-quote 
  {; quote connections
    ;:random {:type :random}
   :bybit {:type :bybit
           :mode :main
           :segment :spot}})

(def accounts-trade
  {;:florian/test1 {:type :bybit
   ;                :mode :test
   ;                :creds (get-creds :bybit/florian)}
   ;:rene/test1 {:type :bybit
   ;             :mode :test
   ;             :creds (get-creds :bybit/rene)}
   ;:rene/test2 {:type :bybit
   ;             :mode :test
   ;             :creds (get-creds :bybit/rene2)}
   
   ;:rene/test3 {:type :bybit
   ;             :mode :test
   ;             :creds (get-creds :bybit/rene3)}
   :rene/test4 {:type :bybit
                :mode :test
                :creds (get-creds :bybit/rene4)}
   })


accounts-trade