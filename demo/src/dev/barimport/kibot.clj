(ns dev.barimport.kibot
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [ta.db.bars.protocol :as b]
   [ta.import.provider.kibot.ds :as k]
   [dev.env :refer [secrets]]))

(def kibot (k/create-import-kibot (:kibot secrets)))

kibot

(def w
  {:start (t/instant "2019-12-01T00:00:00Z")
   :end (t/instant "2021-01-11T00:00:00Z")})


(b/get-bars kibot {:asset "EUR/USD"
                   :calendar [:forex :d]}
            w)

(b/get-bars kibot {:asset "MSFT"
                   :calendar [:us :d]
                   :import :kibot}
            {:start (t/instant "2019-01-01T00:00:00Z")
             :end (t/instant "2021-03-01T00:00:00Z")})
