(ns quanta.market.barimport.bar-matcher
  (:require
   [tablecloth.api :as tc]))

(defn match-window [bar-opentime-ds window]
  (let [interval-ds (-> window
                        (tc/dataset)
                        (tc/rename-columns {:open :date
                                            :close :date-close}))]
    ;interval-ds
    {:ok (-> (tc/inner-join  bar-opentime-ds interval-ds :date)
             (tc/drop-columns [:date])
             (tc/rename-columns {:date-close :date})
             (tc/select-columns [:date :open :high :low :close :volume])
             (tc/order-by [:date] [:asc]))
     :missing (tc/anti-join interval-ds bar-opentime-ds [:date])
     :excess (tc/anti-join bar-opentime-ds interval-ds [:date])}))