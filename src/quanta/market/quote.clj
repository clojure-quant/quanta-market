(ns quanta.market.quote
  (:require
   [quanta.market.protocol :as p]
   ; default quotefeed implementations
   [quanta.market.broker.bybit.quotefeed] ; side effects
   ;[quanta.market.broker.paper.quote] ; side effects
   ))

(defrecord quote-manager [feeds]
  p/subscription-topic
  (get-topic [this sub]
    (let [feed (:feed sub)
          feed (get feeds feed)]
      (p/get-topic feed sub)))
  p/quote
  (trade [this sub]
    (p/get-topic this (assoc sub :topic :asset/trade)))
  (orderbook [this sub]
    (p/get-topic this (assoc sub :topic :asset/orderbook :depth 1))))

(defn- create-feed [[id opts]]
  [id (p/create-quotefeed (assoc opts :feed id))])

(defn- create-feeds [feeds]
  (->> feeds
       (map create-feed)
       (into {})))

(defn quote-manager-start
  "A quote-manager is a quotefeed that multiplexes 
   quotefeeds by :feed key in a get-topic subscription."
  [feeds]
  (let [quotefeeds (create-feeds feeds)]
    (quote-manager. quotefeeds)))

(defn quote-manager-stop [{:keys [feeds] :as this}]
  ;
  )