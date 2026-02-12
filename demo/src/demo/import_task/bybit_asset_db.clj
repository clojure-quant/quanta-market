(ns demo.import-task.bybit-asset-db
  (:require
   [tick.core :as t]
   [clojure.string :as str]
   [missionary.core :as m]
   [quanta.recipy.bybit-asset-db :refer [download-asset-category]]
   [demo.env-bar :refer [ctx]]))

(m/? (download-asset-category ctx {:category "spot"}))
;; => {:category "spot", :assets 621}

(m/? (download-asset-category ctx {:category "linear"}))
; fix it. we cannto reuse the same bybit sybmol, because spot and linear can hav ethe 
; same symbol

;(m/? (download-asset-category ctx {:category "inverse"}))
;; => {:category "inverse", :assets 13}
;(m/? (download-asset-category ctx {:category "option"}))
;; => {:category "option", :assets 500}



