(ns dev.asset.raw.bybit
  (:require
   [tick.core :as t]
   [clojure.string :as str]
   [missionary.core :as m]
   [quanta.market.barimport.bybit.raw :as bb]))

(defn download-save [category]
  (m/sp
   (let [assets (m/? (bb/get-assets category))
         assets (:list assets)]
     (spit (str "../resources/asset/raw/bybit-" category ".edn") assets)
     {:category category
      :assets (count assets)})))

(m/? (download-save "spot"))
;; => {:category "spot", :assets 621}

(m/? (download-save "linear"))
;; => {:category "linear", :assets 466}

(m/? (download-save "inverse"))
;; => {:category "inverse", :assets 13}

(m/? (download-save "option"))
;; => {:category "option", :assets 500}

