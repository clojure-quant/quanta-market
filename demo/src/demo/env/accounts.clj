(ns demo.env.accounts
  (:require [babashka.fs :as fs]))

(defn get-creds [account-id]
  (let [fname (str (System/getenv "MYVAULT") "/goldly/quanta.edn")]
    (if (fs/exists? fname)
      (-> fname
          slurp
          read-string
          account-id)
      (do (println "error: creds file does not exist: " fname)
          {}))))

(def accounts-quote
  {; quote connections
   :random {:type :random}
   :bybit {:type :bybit}})

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
                :creds (get-creds :bybit/rene4)}})



