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

(def websocket-destination-urls
  {:main {:spot "wss://stream.bybit.com/v5/public/spot"
          :future "wss://stream.bybit.com/v5/public/linear" ; USDT, USDC perpetual & USDC Futures
          :inverse "wss://stream.bybit.com/v5/public/inverse"
          :option "wss://stream.bybit.com/v5/public/option" ; USDC Option
          :trade "wss://stream.bybit.com/v5/trade"
          :private "wss://stream.bybit.com/v5/private"}
   :test {:spot "wss://stream-testnet.bybit.com/v5/public/spot"
          :future "wss://stream-testnet.bybit.com/v5/public/linear" ; USDT, USDC perpetual & USDC Futures
          :inverse "wss://stream-testnet.bybit.com/v5/public/inverse"
          :option "wss://stream-testnet.bybit.com/v5/public/option" ; USDC Option
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
    (info "bybit connected!")
    client))

(defn json->msg [json]
  (j/read-value json j/keyword-keys-object-mapper))

(defn msg-flow [!-a]
  ; without the stream the last subscriber gets all messages
  (m/stream
   (m/observe
    (fn [!]
      (info "creating msg-flow reader..")
      (reset! !-a !)
      (fn []
        (info "removing msg-flow reader..")
        (reset! !-a nil))))))

;(future-cancel ping-sender)

(defn connection-start! [opts]
  (info "connection-start..")
  (let [stream (connect! opts)
        !-a (atom nil)
        on-msg (fn [json]
                 (let [msg (json->msg json)]
                   (info "!msg rcvd: " msg)
                   (if @!-a
                     (@!-a msg)
                     (warn "unprocessed msg: " msg))))
        msg-flow (msg-flow !-a)]
    (s/consume on-msg stream)
    (info "connected!")
    {:account opts
     :api :bybit
     :stream stream
     :msg-flow msg-flow}))

(defn connection-stop! [{:keys [stream msg-flow]}]
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

(defn send-msg! [{:keys [stream] :as conn} msg]
  (let [json (j/write-value-as-string msg)]
    (info "send-msg!: " json)
    (if (connected? stream)
      (do @(s/put! stream json)
          (info "send-msg done!")
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
                    (info "target-id: " id "reqId: " reqId "req_id: " req_id)
                    (or (= id reqId) (= id req_id)))
          result (first-match p-reqId (:msg-flow conn))
          msg (assoc msg :reqId id "req_id" id)]
      (info "making rpc request:  " msg)
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