(ns quanta.market.broker.bybit.quotefeed
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.quote.quotefeed-category] ; side effects
   [quanta.market.broker.bybit.asset :refer [asset-category]]))

(defrecord bybit-feed [feeds]
  p/subscription-topic
  (get-topic [this sub]
    (let [asset (:asset sub)
          {:keys [bybit-symbol category]} (asset-category asset)
          category-kw (keyword category)
          _ (debug "asset:" asset " category: " category-kw "bb-symbol: " bybit-symbol)
          feed (get feeds category-kw)
          sub-category (assoc sub :asset bybit-symbol)]
      (p/get-topic feed sub-category)))
  p/quote
  (trade [this sub]
    (p/get-topic this (assoc sub :topic :asset/trade)))
  (orderbook [this sub]
    (p/get-topic this (assoc sub :topic :asset/orderbook :depth 1))))

(def categories
  {:spot {:mode :main
          :segment :spot}
   :linear {:mode :main
            :segment :linear}
   :inverse {:mode :main
             :segment :inverse}
   :option {:mode :main
            :segment :options}})

(defn make-feed [[kw opts]]
  [kw (p/create-quotefeed (assoc opts :type :bybit-category))])

(defn make-feeds []
  (->> categories
       (map make-feed)
       (into {})))

(defmethod p/create-quotefeed :bybit
  [opts]
  (info "creating bybit quotefeed ..")
  (bybit-feed. (make-feeds)))

(comment

  (make-feeds)

 ; 
  )


