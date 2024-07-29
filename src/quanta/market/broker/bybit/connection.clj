(ns quanta.market.broker.bybit.connection
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [nano-id.core :refer [nano-id]]
   [jsonista.core :as j] ; json read/write
   [aleph.http :as http]
   [manifold.stream :as s] ; websocket to bybit
   [quanta.market.util :refer [first-match]]))

;; https://bybit-exchange.github.io/docs/v5/ws/connect

(def mode
  {:main "wss://stream.bybit.com/v5/"
   :test "wss://stream-testnet.bybit.com/v5/"})

(def segment
  {:data "public/"
   :trade "trade"
   :order-update "private"})

(def asset-class
  {:spot "spot" ; BTCUSD  BTCUSDT
   :future "linear" ; BTC-26JUL24 BTC-30AUG24 BTC-PERP
   :future-inverse "inverse" ;  BTCUSD           BTCUSDZ24  BTCUSDU24 
   :option "option"})

(def websocket-destination-urls
  {:main {; data
          :spot "wss://stream.bybit.com/v5/public/spot"
          :linear "wss://stream.bybit.com/v5/public/linear" ; USDT, USDC perpetual & USDC Futures
          :inverse "wss://stream.bybit.com/v5/public/inverse"
          :option "wss://stream.bybit.com/v5/public/option" ; USDC Option
          ; trade
          :trade "wss://stream.bybit.com/v5/trade"
          :private "wss://stream.bybit.com/v5/private"}
   :test {; data
          :spot "wss://stream-testnet.bybit.com/v5/public/spot"
          :linear "wss://stream-testnet.bybit.com/v5/public/linear" ; USDT, USDC perpetual & USDC Futures
          :inverse "wss://stream-testnet.bybit.com/v5/public/inverse"
          :option "wss://stream-testnet.bybit.com/v5/public/option" ; USDC Option
          ; trade
          :trade "wss://stream-testnet.bybit.com/v5/trade"
          :private "wss://stream-testnet.bybit.com/v5/private"}})

(defn get-ws-url [mode destination]
  (get-in websocket-destination-urls [mode destination]))

(defn connect! [{:keys [mode segment]}]
  (let [url (get-ws-url mode segment)
        _ (info "bybit connect mode: " mode " segment: " segment " url: "  url)
        client @(http/websocket-client url)
        ;f (set-interval (gen-ping-sender client) 5000)
        ]
    (debug "bybit connected!")
    client))

(defn json->msg [json]
  (j/read-value json j/keyword-keys-object-mapper))

(defn connection-start! [flow-sender-in flow-sender-out opts]
  (debug "connection-start..")
  (let [stream (connect! opts)
        send-in-fn (:send flow-sender-in)
        send-out-fn (:send flow-sender-out)
        on-msg (fn [json]
                 (let [msg (json->msg json)]
                   (debug "!msg rcvd: " (:account-id opts) " " msg)
                   (send-in-fn msg)))
         _ (assert send-in-fn "send-in-fn must be defined")
         _ (assert send-out-fn "send-out-fn must be defined")    
         stream-consumer (s/consume on-msg stream)]
    (info (:account-id opts) " connected!")
    {:account opts
     :opts opts
     :api :bybit
     :stream stream
     :send-in-fn send-in-fn
     :send-out-fn send-out-fn
     :msg-in-flow (:flow flow-sender-in)
     :msg-out-flow (:flow flow-sender-out)
     :stream-consumer stream-consumer
     }))

(defn connection-stop! 
   "close a websocket connection. 
    input is the state map you get on connection-start!"
  [{:keys [stream]}]
  (info "connection-stop.. ")
  (.close stream))

(defn info? [conn]
  (let [stream (:stream conn)]
    (let [desc (s/description stream)]
      desc)))

(defn connected? [stream]
  (when stream
    (let [desc (s/description stream)]
      (and
       (not (-> desc :sink :closed?))
       (not (-> desc :source :closed?))))))

(defn connected2? [{:keys [stream] :as conn}]
  (when stream
    (let [desc (s/description stream)]
      (and
       (not (-> desc :sink :closed?))
       (not (-> desc :source :closed?))))))


(defn send-msg! [{:keys [stream send-out-fn] :as conn} msg]
  (let [json (j/write-value-as-string msg)]
    (info "send-msg!: " json)
    (if (connected? stream)
      (do @(s/put! stream json)
          (debug "send-msg done!")
          (send-out-fn msg)
          :send-success)
      (do
        (error "send-msg failed (no connection): " msg)
        (throw (ex-info "not connected" {:send-msg msg}))))))

(defn send-msg-task [conn msg]
  (m/sp
   (m/? (m/sleep 100))
   (send-msg! conn msg)))

(defn get-result [send-result reply]
  (info "send-result: " send-result)
  (info "reply: " reply)
  reply)

(defn rpc-req! [conn msg]
  (if conn
    (let [id (nano-id 8)
          p-reqId (fn [{:keys [reqId req_id]}]
                    (debug "target-id: " id "reqId: " reqId "req_id: " req_id)
                    (or (= id reqId) (= id req_id)))
          result (first-match p-reqId (:msg-in-flow conn))
          msg (assoc msg :reqId id
                     "req_id" id ; this is importan for quote-subscriptions.
                     )]
      (debug "making rpc request:  " msg)
      (let [r (m/join get-result
                      (send-msg-task conn msg)
                      result)]
        (m/race r
                (m/sleep 5000 {:error "request timeout after 5 seconds"
                               :msg msg}))))
    (throw (ex-info "not connected" {:send-msg msg}))))

(comment
   ;raw websocket testing:

  (def c (connect! {:mode :main
                    :segment :spot}))
  (s/consume println c)

  c
  (.close c)

;  
  )