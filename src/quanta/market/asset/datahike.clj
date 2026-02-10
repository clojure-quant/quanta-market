(ns quanta.market.asset.datahike
  (:require
   [taoensso.timbre :as timbre :refer [info warn error]]
   [clojure.java.io :as io]
   [datahike.api :as d]
   [quanta.market.asset.schema :refer [schema]]))

(defn start-asset-db [db-dir]
  (let [cfg {:store {:backend :file ; backends: in-memory, file-based, LevelDB, PostgreSQL
                     :path db-dir}
             :keep-history? false
             :schema-flexibility :write  ;default - strict value types need to be defined in advance. 
                  ;:schema-flexibility :read ; transact any  kind of data into the database you can set :schema-flexibility to read
             :initial-tx schema ; commit a schema
             }]
  ; create when not existing
  ; (.exists (io/file db-filename))
    (when-not (d/database-exists? cfg)
      (warn "creating datahike db..")
    ;(d/delete-database cfg)
      (d/create-database cfg))
  ; connect
    (d/connect cfg)))

(defn stop [conn]
  (when conn
    (info "disconnecting from datahike..")
    (d/release conn)
    (info "datahike stopped!")))

(defn add-asset-details [dbconn asset]
  (d/transact dbconn [(merge {:db/id [:asset/symbol (:asset/symbol asset)]}
                             asset)]))


(defn- conj-key-when [query k v]
  (if v
    (conj query `[~'?id ~k ~v])
    query))

(defn- conj-q-when [query q]
  (if q
    (let [r (re-pattern (str "(?i)" q))]
      (-> query
          (conj `[~'?id :asset/name ~'?n])
          (conj `[(re-find ~r ~'?n)])
          ;(conj  `[~'?id :asset/symbol ~'?s])
          ;(conj  `[(or (re-find ~r ~'?n) (re-find ~r ~'?s))])))
          ))
    query))

(defn query-assets [dbconn {:keys [q exchange type]}]
  (-> '[:find [(pull ?id [*]) ...]
        :in $
        :where
        [?id :asset/symbol _]]
      (conj-key-when :asset/type type)
      (conj-key-when :asset/exchange exchange)
      (conj-q-when q)
      (d/q @dbconn)))


(defn add-update-asset 
  "[{:asset/symbol \"SPY\"
                  :asset/name \"Spiders S&P 500 ETF\"
                  :asset/exchange :NYSE
                  :asset/type :etf}]"
  [dbconn asset]
  (if (map? asset)
    (d/transact dbconn [asset])
    (d/transact dbconn asset)))

