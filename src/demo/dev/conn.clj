(ns demo.dev.conn)

(require '[clojure.edn :refer [read-string]])
(def creds
  (-> (System/getenv "MYVAULT")
      (str "/goldly/quanta.edn")
      slurp
      read-string
      :bybit/test))

(def account {:mode :test
              :segment :trade
              :account creds})

account
(def conn
  (c/connection-start! account))

conn

(c/info? conn)


 (m/?  (authenticate! conn account))