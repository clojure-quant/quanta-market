(ns demo.barimport.eodhd-splits
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
   [quanta.market.barimport.eodhd.ds :refer [create-import-eodhd get-splits]]
   [quanta.market.barimport.eodhd.raw :as raw]
   [demo.env-bar :refer [secrets eodhd bardb-nippy eodhd-token]]
   [tablecloth.api :as tc]))



   (let [bar-ds (m/? (b/get-bars eodhd
                                 {:asset asset
                                  :calendar [:us :d]}
                                 {:start (t/zoned-date-time "2000-01-01T00:00:00Z")
                                  :end (t/zoned-date-time "2026-03-20T00:00:00Z")}))]
     

     [{:date "2026-02-06", :split "1.000000/25.000000", :code "ADIL", :exchange "US"}
      {:date "2026-02-06", :split "1.000000/20.000000", :code "ASST", :exchange "US"}
      {:date "2026-02-06", :split "0.100000/1.000000", :code "HNATD", :exchange "US"}
      {:date "2026-02-06", :split "0.100000/1.000000", :code "HNATF", :exchange "US"}
      {:date "2026-02-06", :split "1.000000/5.000000", :code "PRFX", :exchange "US"}
      {:date "2026-02-06", :split "1.000000/8.000000", :code "RLYB", :exchange "US"}]
     

     (m/? (raw/get-splits
           eodhd-token
           {:asset "MSFT.US" :from "1980-01-01" :to "2026-03-20"}))
     
  [{:date "1987-09-21", :split "2.000000/1.000000"}
   {:date "1990-04-16", :split "2.000000/1.000000"}
   {:date "1991-06-27", :split "3.000000/2.000000"}
   {:date "1992-06-15", :split "3.000000/2.000000"}
   {:date "1994-05-23", :split "2.000000/1.000000"}
   {:date "1996-12-09", :split "2.000000/1.000000"}
   {:date "1998-02-23", :split "2.000000/1.000000"}
   {:date "1999-03-29", :split "2.000000/1.000000"}
   {:date "2003-02-18", :split "2.000000/1.000000"}]

     (m/? (get-splits eodhd-token {:asset "MSFT"} {}))
    