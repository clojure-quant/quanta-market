(ns quanta.market.asset.schema)

; see: https://github.com/replikativ/datahike/blob/main/doc/schema.md

; :db/valueType
; :db.type/boolean 
; :db.type/string
; :db.type/long :db.type/number :db.type/double :db.type/float db.type/bigint db.type/bigdec
; :db.type/keyword :db.type/symbol
; :db.type/ref
; :db.type/instant 	java.util.Date
; :db.type/uuid
; :db.type/bytes

; db/ident, , :db/cardinality

; optional
; db/index true/false

(def asset
  [{:db/ident :asset/symbol
    :db/unique :db.unique/identity ; symbol is our id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :asset/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :asset/type
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one}
   {:db/ident :asset/exchange
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   ])

(def lists
  [{:db/ident :lists/name
    :db/unique :db.unique/identity ; name of list is our id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :lists/asset
    :db/valueType :db.type/tuple
    :db/tupleTypes [:db.type/long :db.type/string] ;; [idx value]
    :db/cardinality :db.cardinality/many}])


(def schema
  (->> (concat asset lists)
       (into [])))