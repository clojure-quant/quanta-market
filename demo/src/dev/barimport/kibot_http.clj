(ns dev.barimport.kibot-http
  (:require
   [missionary.core :as m]
   [ta.db.asset.db :as db]
   [ta.db.bars.protocol :refer [get-bars]]
   [quanta.market.barimport.kibot.http :as kibot-http]
   [quanta.market.barimport.kibot.raw :refer [get-web]]

   [dev.env :refer [secrets]]))

; test if EURUSD has kibot-http
(db/instrument-details "EURUSD")
(m/? (a/http-get base-url query-params))

(def s (:kibot secrets))
s
;; => {:user "hoertlehner@gmail.com", :password "282m2fhgh"}

(def k (kibot-http/create-import-kibot-http (:kibot secrets)
                                            ;; => {:user "hoertlehner@gmail.com", :password "282m2fhgh"}
                                            ))
k
(m/? (get-bars
      k
      {:asset "EURUSD"
       :calendar [:us :m]}
      :full))

?product=