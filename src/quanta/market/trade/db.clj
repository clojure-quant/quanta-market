(ns quanta.market.trade.db
  (:require
   [taoensso.timbre :as timbre :refer [info warn error]]
   [tick.core :as t]
   [clojure.java.io :as io]
   [datahike.api :as d]))

(def schema
  [; message
   {:db/ident :message/timestamp
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one}
   {:db/ident :message/direction
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one}
   {:db/ident :message/account
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one}
   {:db/ident :message/asset
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :message/data
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
  ; order
  {:db/ident :order/account
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one}
  {:db/ident :message/asset
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one}
  {:db/ident :order/side
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one}
  {:db/ident :order/quantity
   :db/valueType :db.type/double
   :db/cardinality :db.cardinality/one}
  {:db/ident :order/type
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one}
  {:db/ident :order/limit
   :db/valueType :db.type/double
   :db/cardinality :db.cardinality/one}
  {:db/ident :order/date-created
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one}
  {:db/ident :order/date-done
   :db/valueType :db.type/instant
   :db/cardinality :db.cardinality/one}
   {:db/ident :order/status
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one}
  ; trade
  {:db/ident :trade/account
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one}
  {:db/ident :trade/order
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one}
  {:db/ident :trade/asset
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one}
  {:db/ident :trade/side
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one}
  {:db/ident :trade/quantity
   :db/valueType :db.type/double
   :db/cardinality :db.cardinality/one}
  {:db/ident :trade/price
   :db/valueType :db.type/double
   :db/cardinality :db.cardinality/one}
   {:db/ident :trade/date
   :db/valueType :db.type/instant
   :db/cardinality :db.cardinality/one}]
  )

(defn- cfg [path]
  {:store {:backend :file ; backends: in-memory, file-based, LevelDB, PostgreSQL
           :path path}
   :keep-history? false
   :schema-flexibility :write  ;default - strict value types need to be defined in advance. 
   ;:schema-flexibility :read ; transact any  kind of data into the database you can set :schema-flexibility to read
   :initial-tx schema ; commit a schema
   })

(defn- create! [cfg]
  (warn "creating datahike db..")
  (d/delete-database cfg)
  (d/create-database cfg)
  (d/connect cfg))

(defn trade-db-start [path]
  (let [cfg (cfg path)
        db-filename (get-in cfg [:store :path])]
    (info "trade-db starting at path: " db-filename)
    (if (.exists (io/file db-filename))
      (d/connect cfg)
      (create! cfg))))

(defn trade-db-stop [conn]
  (when conn
    (info "trade-db stopping ..")
    (d/release conn)))

(defn store-message! [conn account direction data]
  (info "storing message for account: " account " data:" data)
  (let [tx {:message/timestamp (t/inst)
            :message/direction direction
            :message/account account
            :message/data (pr-str data)}]
    (d/transact conn [tx])))

(defn query-messages
  [conn {:keys [account]}]
   (d/q '[:find [(pull ?msg [:message/timestamp
                            :message/direction
                            :message/account
                            :message/data]) ...]
           :in $ account
           :where [?msg :message/account account]]
         @conn account))
