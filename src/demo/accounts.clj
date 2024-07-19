(ns demo.accounts)


(defn get-creds [account-id]
  (-> (System/getenv "MYVAULT")
      (str "/goldly/quanta.edn")
      slurp
      read-string
      account-id))

(def accounts
  {; quote connections
    ;:random {:type :random}
   :bybit {:type :bybit
           :mode :main
           :segment :spot}
   :florian/test1 {:type :bybit
                   :mode :test
                   :segment :trade
                   :creds (get-creds :bybit/florian)}
   :rene/test1 {:type :bybit
                :mode :test
                :segment :trade
                :creds (get-creds :bybit/rene)}
   :rene/test1-orderupdate {:type :bybit
                            :mode :test
                            :segment :private
                            :creds (get-creds :bybit/rene)}})

accounts