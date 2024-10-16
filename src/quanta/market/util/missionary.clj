(ns quanta.market.util.missionary
  (:require
   [missionary.core :as m]))

(defn backoff [request delays]
  (if-some [[delay & delays] (seq delays)]
    (m/sp
     (try (m/? request)
          (catch Exception e
            (if (-> e ex-data :worth-retrying)
              (do (m/? (m/sleep delay))
                  (m/? (backoff request delays)))
              (throw e)))))
    request))