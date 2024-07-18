(ns demo.accounts)


 (def bybit-test-creds
   (-> (System/getenv "MYVAULT")
       (str "/goldly/quanta.edn")
       slurp
       read-string
       :bybit/test))
 
 (def accounts
   {; quote connections
    ;:random {:type :random}
    :bybit {:type :bybit
            :mode :main
            :segment :spot}
    :florian/test1 {:type :bybit
                    :mode :test
                    :segment :trade
                    :creds bybit-test-creds}
      ; trade connections
    })