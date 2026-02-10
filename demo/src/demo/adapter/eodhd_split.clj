(ns demo.adapter.eodhd-splits
  (:require
   [clojure.pprint :refer [print-table]]
   [tick.core :as t]
   [missionary.core :as m]
   [tablecloth.api :as tc]
   [modular.persist.edn] ; side effects to load edn files
   [modular.persist.protocol :refer [save loadr]]
   [quanta.bar.protocol :as b :refer [bardb barsource]]
   [quanta.calendar.window :refer [date-range->window
                                   window->close-range
                                   window->open-range
                                   window->intervals]]
   [quanta.bar.split.adjust :refer [add-split-factor-linear split-adjust]]
   [quanta.market.adapter.eodhd.ds :refer [create-import-eodhd get-splits]]
   [quanta.market.adapter.eodhd.raw :as raw]
   [demo.env-bar :refer [eodhd bardb-nippy eodhd-token]]))

(def split-ds
  (m/? (get-splits eodhd-token {:asset "MSFT"} {:from (t/date "1980-01-01")
                                                :to (t/date "2026-03-20")})))

split-ds
;|                :date | :factor |
;|----------------------|--------:|
;| 1987-09-21T00:00:00Z |     2.0 |
;| 1990-04-16T00:00:00Z |     2.0 |
;| 1991-06-27T00:00:00Z |     1.5 |
;| 1992-06-15T00:00:00Z |     1.5 |
;| 1994-05-23T00:00:00Z |     2.0 |
;| 1996-12-09T00:00:00Z |     2.0 |
;| 1998-02-23T00:00:00Z |     2.0 |
;| 1999-03-29T00:00:00Z |     2.0 |
;| 2003-02-18T00:00:00Z |     2.0 |

(def bar-ds (m/? (b/get-bars eodhd
                             {:asset "MSFT"
                              :calendar [:us :d]}
                             {:start (t/zoned-date-time "1980-01-01T00:00:00Z")
                              :end (t/zoned-date-time "2026-03-01T00:00:00Z")})))

(-> bar-ds
    (add-split-factor-linear split-ds)
    (tc/select-columns [:date :close :volume :adjusted_close :factor]))
;|                :date | :close |  :volume | :adjusted_close | :factor |
;|----------------------|-------:|---------:|----------------:|--------:|
;| 2003-02-13T00:00:00Z |  46.99 | 73839524 |         14.3385 |     2.0 |
;| 2003-02-14T00:00:00Z |  48.30 | 90770868 |         14.7382 |     2.0 |
;| 2003-02-18T00:00:00Z |  24.96 | 57543711 |         15.2325 |     1.0 |

(-> bar-ds
    (split-adjust split-ds)
    (tc/select-columns [:date :close :volume :adjusted_close :factor]))

(let [ds (split-adjust bar-ds split-ds)]
  (-> (tc/add-column ds :ratio
                     (map (fn [a b]
                            (/ a b)) (:close ds) (:adjusted_close ds)))
      (tc/info)))

               