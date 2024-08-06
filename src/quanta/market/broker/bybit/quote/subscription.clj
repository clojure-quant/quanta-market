(ns quanta.market.broker.bybit.quote.subscription)

; https://bybit-exchange.github.io/docs/v5/websocket/public/trade

(defn subscription-start-msg [topic]
  {"op" "subscribe"
   "args" [topic]})

(defn subscription-stop-msg [topic]
  {"op" "unsubscribe"
   "args" [topic]})

(def subscription-success-demo
  {"success" true
   "ret_msg" "subscribe"
   "conn_id" "cf71cb32-e914-40db-9710-ac45c8086cae"
   "req_id" "6"
   "op" "subscribe"})

(def subsciption-err-execution
  {:op "subscribe"
   :reqId "IN7XkfuD"
   :retCode 10404
   :retMsg ""
   :connId "cpv86i6c0hvd5nkl25n0-2x3h"})

(def subscription-err-ticketinfo
  {:op "subscribe",
   :success false,
   :conn_id "cq1814tdaugt75sdcg8g-22tvw",
   :ret_msg "Batch subscription partially succeeded and partially failed.Successful subscriptions are as follows:[]. Subscription to the following topics failed because the topic does not exist or there is a subscription conflict:[ticketInfo]."
   :req_id "Tg7vi8n1"})

(def subscription-success-order
  {:op "subscribe"
   :success true
   :conn_id "cq1814tdaugt75sdcg8g-22tvw"
   :ret_msg ""
   :req_id "W-goZgZQ"})

