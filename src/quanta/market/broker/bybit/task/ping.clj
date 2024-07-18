(ns quanta.market.broker.bybit.task.ping
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [quanta.market.broker.bybit.connection :as c]))


(def ping-req-example 
  {"req_id" "100001"
   "op" "ping"})

(def ping-success-example
  {:retCode 0
   :retMsg "OK"
   :connId "cpv86i6c0hvd5nkl25n0-2wnh"
   :op "pong"
   :data ["1721313135879"]})

(def ping-response-demo
  {"success" true
   "ret_msg" "pong"
   "conn_id" "85ea7772-1b16-4d76-ace2-4ac7e7b6d163"
   "req_id" "100001"
   "op" "ping"})


(defn ping! [conn]
  (let [msg {"op" "ping"}]
    (debug "ping! ")
    (c/rpc-req! conn msg)))