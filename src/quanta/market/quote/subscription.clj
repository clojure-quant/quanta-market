(ns quanta.market.quote.subscription
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.broker.bybit.websocket2 :as ws]
   [quanta.market.util :as util])
  (:import [missionary Cancelled]))

(defrecord sub-man [opts websocket]




(defn create-subscription-manager [qf]
  {:qf qf
   :
   :})

(defmethod p/create-quotefeed :bybit2
  [opts]
  (info "creating bybit quotefeed : " opts)
  (let [opts (merge {:mode :main
                     :segment :spot} opts)
        websocket (ws/create-websocket opts)
        subscriptions (atom {})
        lock (m/sem)
        ]
    (sub-man. opts websocket subscriptions lock)))


(comment
  @subscriptions
  (reset! subscriptions {})

 ; 
  )

