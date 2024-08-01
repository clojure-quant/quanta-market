(ns quanta.market.broker.bybit.quotefeed-category
  (:require 
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.quote.subscription :refer [create-topic-subscriber]]
   [quanta.market.broker.bybit.websocket2 :refer [create-websocket2]]
   [quanta.market.broker.bybit.task.subscription :as s]
   [quanta.market.broker.bybit.topic :refer [format-topic-sub topic-data-flow topic-transformed-flow]]
   ))

(defrecord bybit-subscriber [websocket]
  p/connection-subscriber
  (get-conn [_this]
    (info "bybit subscriber returning websocket: " websocket)
     websocket)
  (subscription-start! [_this conn sub]
    (let [topic (format-topic-sub sub)]
      (s/subscription-start! conn topic)))
  (subscription-stop! [_this conn sub]
    (let [topic (format-topic-sub sub)]
      (s/subscription-stop! conn topic)))
  (topic-view [this sub]
    (let [c (p/get-conn this)
          _ (info "getting msg-in-flow for c: " c)
          msg-in (p/msg-in-flow c)
          topic (format-topic-sub sub)
          topic-data-f (topic-data-flow msg-in topic)]
      (topic-transformed-flow topic-data-f topic))))

(defmethod p/create-quotefeed :bybit-category
  [opts]
  (info "wiring up bybit-category feed : " opts)
  (let [opts (merge {:mode :main
                     :segment :spot} opts)
        websocket (create-websocket2 opts)
        subscriber (bybit-subscriber. websocket)]
    (create-topic-subscriber subscriber)))



(comment
  (try
    (/ 200.0 6.0)
    (catch Exception e (str "caught exception: " (.getMessage e)))))