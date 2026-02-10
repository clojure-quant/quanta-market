

(ns crb.db.store.simple
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   [crb.db.datahike :as dhdb]
   [crb.db.store.diff :refer [new-or-changed]]))

; search if db/id is existing

(def query-entity-id
  '[:find [?id]
    :in $ db-attr id-val
    :where
    [?id db-attr id-val]])

(defn existing-db-id [db-attr id-val]
  (debug "search id col:" db-attr " id: " id-val)
  (let [d (dhdb/q query-entity-id db-attr id-val)]
    (when d
      (first d))))

; create tx

(defn create-tx-add [data-new]
  (when-not (empty? data-new)
    (let [id-db (dhdb/tempid)]
      {:txs [(assoc data-new :db/id id-db)]
       :action :add
       :id id-db})))

;(defn create-tx-update-full [id-db data-new]
;  (assoc data-new :db/id id-db))

(defn create-tx-update-diff [id-db data-new]
  (let [data-old (dhdb/pull '[*] id-db)
        data-chg (new-or-changed data-old data-new)]
    (if (empty? data-chg)
      {:txs []
       :action :unchanged
       :id id-db}
      {:txs [(assoc data-chg :db/id id-db)]
       :action :update
       :id id-db})))

(defn create-tx [id-db-key data-dh]
  (if-let [id-db (existing-db-id id-db-key (id-db-key data-dh))]
    (create-tx-update-diff id-db data-dh)
    (create-tx-add data-dh)))