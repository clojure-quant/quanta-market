(ns quanta.notebook.asset-db.datahike
  (:require
   [quanta.market.asset.datahike :refer [add-update-asset query-assets get-asset
                                         add-update-list get-list
                                         provider->asset asset->provider
                                         exchanges categories]]
   [modular.system :refer [system]]))

(def asset-db (get-in system [:ctx :asset-db]))

{:symbol "ENSUSDT.BB"
 :name "ENSUSDT"
 :exchange "BYBIT"
 :category :crypto
 :bybit "ENSUSDT"
 :bybit-category :spot}

;; details on single asset

(get-asset asset-db "MSFT")

(get-asset asset-db "000")

(asset->provider asset-db :bybit "ENSUSDT.BB")
;; [:spot "ENSUSDT"]
(provider->asset asset-db :bybit [:spot "ENSUSDT"])
;; "ENSUSDT.BB"

(exchanges asset-db)

(categories asset-db)

(query-assets asset-db {:q "MSFT"})

;; lists 
(-> (add-update-list asset-db {:lists/name "flo" :lists/asset ["QQQ" "MSFT" "SPY"]})
    nil)

(get-list asset-db "flo")

(-> (get-list asset-db "etf-10mio")
    :lists/asset
    count)

(get-list asset-db "etf-10mio")
;; asset list now contains duplicates.

(-> (get-list asset-db "equity-20mio")
    :lists/asset
    count)

(-> (get-list asset-db "equity-20mio")
    :lists/asset
    count)

