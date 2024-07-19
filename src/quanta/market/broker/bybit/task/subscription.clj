(ns quanta.market.broker.bybit.task.subscription
   (:require
    [taoensso.timbre :as timbre :refer [debug info warn error]]
    [missionary.core :as m]
    [quanta.market.broker.bybit.connection :as c]))

; https://bybit-exchange.github.io/docs/v5/websocket/public/trade

(def topics
  {:order/execution "ticketInfo"
   :order/update "order"
    ; market
   :asset/orderbook "orderbook.%s.%s" ; depth asset OK
   :asset/orderbook-top  "bookticker.%s" ; best bid ask every 100ms  NO
   :asset/bars "kline.%s.%s" ; interval asset ; OK
   :asset/stats "tickers.%s" ; OK
   :asset/trade "publicTrade.%s"  ; symbol realtime  OK
   :asset/liquidation "liquidation.%s" ; BAD
   })

(defn topic [type args]
  (if-let [s (get topics type)]
    (apply format s args)
    (throw (Exception. (ex-info "topic not found" {:type type
                                                   :args args})))))

(defn subscription-start-msg [topic]
  {"op" "subscribe"
   "args" [topic]})

(defn subscription-start!
  [conn sub-type & args]
  (let [t (topic sub-type args)]
    (info "subscription-start topic: " t " ..")
    (c/rpc-req! conn (subscription-start-msg t))))

(defn subscription-stop-msg [topic]
  {"op" "unsubscribe"
   "args" [topic]})

(defn subscription-stop!
  [conn sub-type & args]
  (let [t (topic sub-type args)]
    (info "subscription-stop topic: " t " ..")
    (c/rpc-req! conn (subscription-stop-msg t))))


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
 :connId "cpv86i6c0hvd5nkl25n0-2x3h"
 })

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



(comment
  (def conn
    (c/connection-start! {:mode :main
                          :segment :spot}))
  
  (c/connection-stop! conn)

  (m/? (subscription-start!
        conn
        :asset/stats "BTCUSDT"))
  
  (m/? (subscription-stop!
        conn
        :asset/stats "BTCUSDT"))
  
  
;
  
)


  