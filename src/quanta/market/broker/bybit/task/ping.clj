(ns quanta.market.broker.bybit.task.ping
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.broker.bybit.connection :as c]))


(def ping-success-example
  {:retCode 0
   :retMsg "OK"
   :connId "cpv86i6c0hvd5nkl25n0-2wnh"
   :op "pong"
   :data ["1721313135879"]})


(defn ping! [conn]
  (let [msg {"op" "ping"}]
    (info "ping! ")
    (c/rpc-req! conn msg)))