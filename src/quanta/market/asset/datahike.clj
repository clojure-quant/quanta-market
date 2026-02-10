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
