(ns quanta.notebook.asset-db.datahike-edit
  (:require
   [datahike.api :as d]
   [quanta.market.asset.datahike :refer [add-update-asset query-assets get-asset
                                         add-update-list get-list
                                         provider->asset asset->provider
                                         exchanges categories]]
   [modular.system :refer [system]]))

(def asset-db (get-in system [:ctx :asset-db]))

;; add/modify asset

(add-update-asset asset-db {:asset/symbol "AAPL"
                            :asset/name "Apple Computer Inc"})

(add-update-asset asset-db [{:asset/symbol "AAPL"
                             :asset/name "Apple Computer Inc"}])

(add-update-asset asset-db [{:asset/symbol "AAPL"
                             :asset/exchange "NASDAQ"
                             :asset/category :equity}])

(add-update-asset asset-db [{:asset/symbol "MO"
                             :asset/name "Altria"
                             :asset/exchange "NYSE"
                             :asset/category :equity}])

(add-update-asset asset-db [{:asset/symbol "SPY"
                             :asset/name "Spiders S&P 500 ETF"
                             :asset/exchange "NYSE"
                             :asset/category :etf}])

(add-update-asset asset-db [{:asset/symbol "SPY"
                             :asset/name "Spiders S&P 500 ETF"
                             :asset/exchange "NYSE"
                             :asset/category :etf}])