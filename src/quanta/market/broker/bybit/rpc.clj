(ns quanta.market.broker.bybit.rpc
  (:require
    [taoensso.timbre :as timbre :refer [debug info warn error]]
    [missionary.core :as m]
    [nano-id.core :refer [nano-id]]
    [quanta.market.util :refer [mix current-v first-match flow-sender]]
    [quanta.market.broker.bybit.connection :as c]
    [quanta.market.protocol :as p]))


(defn conn [websocket]
  (debug "getting connection for websocket..")
  (let [conn-f (p/connection-flow websocket)]
    (current-v conn-f)))

(defn send-msg-t [websocket id msg]
  (let [conn-t (conn websocket)
        msg (assoc msg :reqId id
                   "req_id" id ; this is important for quote-subscriptions.
                   )]
    (info "sending msg: " msg)
    (m/sp
     (let [conn (m/? conn-t)]
        (info "send-msg got an connection! conn:" conn)
        (m/? (m/sleep 50)) ; so we subscribe to the messages in get-result-t first.
        (m/? (c/send-msg-task! conn msg))))))


(defn get-result-t [websocket id]
  (info "awaiting result req-id: " id)
  (let [msg-in-f (p/msg-in-flow websocket)
        p-reqId (fn [{:keys [reqId req_id]}]
                  (debug "target-id: " id "reqId: " reqId "req_id: " req_id)
                  (or (= id reqId) (= id req_id)))]
    (first-match p-reqId msg-in-f)))


(defn get-result [result-fn]
  (fn [send-result reply]
    (let [reply2 (result-fn reply)]
      (info "send-result-raw: " send-result
            "reply-raw: " reply
            "reply2: " reply2)
      reply2)))

(defn rpc-req! [websocket msg result-fn]
  (let [req-id (nano-id 8)]
    (debug "making rpc request:  " msg)
    (m/join (get-result result-fn)
            (m/race
             (send-msg-t websocket req-id msg)
             (m/sleep 10000 {:error "message could not be sent in 10 seconds"
                            :msg msg}))
            (m/race
             (get-result-t websocket req-id)
             (m/sleep 15000 {:error "message sent, but no reply in 15 seconds."
                             :msg msg})))))
