(ns quanta.market.quote.core
  (:require
   [missionary.core :as m]
   [quanta.market.util :refer [current-v]]
   [quanta.market.protocol :as p]))


(defn topic-snapshot [qm {:keys [feed asset timeout topic]
                          :or {timeout 5000
                               topic :asset/trade}
                          :as sub}]
  (let [qsub (dissoc sub :timeout)
        _ (println "sub: " qsub)
        topic-f (p/get-topic qm qsub)]
    (m/race
     (m/sleep 5000 :timeout)
     (current-v topic-f))))






