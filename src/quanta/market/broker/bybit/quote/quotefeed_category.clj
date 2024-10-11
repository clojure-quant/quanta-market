(ns quanta.market.broker.bybit.quote.quotefeed-category
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.protocol :as p]
   [quanta.market.quote.subscription :refer [create-topic-subscriber]]
   [quanta.market.broker.bybit.websocket2 :refer [create-websocket2]]
   [quanta.market.broker.bybit.quote.subscription :refer [subscription-start-msg subscription-stop-msg]]
   [quanta.market.broker.bybit.topic :refer [format-topic-sub topic-data-flow topic-transformed-flow]]
   [quanta.market.broker.bybit.rpc :refer [rpc-req!]]))

(defrecord bybit-subscriber [websocket]
  p/connection-subscriber
  (get-conn [_this]
    websocket)
  (subscription-start! [_this conn sub]
    (let [topic (format-topic-sub sub)
          msg (subscription-start-msg topic)]
      (info "sub start topic: " topic)
      (rpc-req! websocket msg identity)))
  (subscription-stop! [_this conn sub]
    (let [topic (format-topic-sub sub)
          msg (subscription-stop-msg topic)]
      (info "sub stop topic: " topic)
      (rpc-req! websocket msg identity)))
  (topic-view [this sub]
    (info "topic view sub: " sub)
    (let [c (p/get-conn this) ; c is the websocket.
          ;_ (info "getting msg-in-flow for c: " c)
          msg-in (p/msg-in-flow c)
          topic (format-topic-sub sub)
          topic-data-f (topic-data-flow msg-in topic)]
      (info "returning topic-transformed-flow..")
      (topic-transformed-flow topic-data-f sub))))

(defmethod p/create-quotefeed :bybit-category
  [opts]
  (info "wiring up bybit-category feed : " opts)
  (let [opts (merge {:mode :main
                     :segment :spot} opts)
        label [:bybit (:segment opts) (or (:feed opts) (:account opts))]
        websocket (create-websocket2 opts label)
        subscriber (bybit-subscriber. websocket)]
    (create-topic-subscriber subscriber)))

(comment
  (try
    (/ 200.0 6.0)
    (catch Exception e (str "caught exception: " (.getMessage e)))))