(ns quanta.market.broker.bybit.task.auth
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [buddy.core.codecs :as crypto.codecs] ; authentication
   [buddy.core.mac :as crypto.mac]
   [quanta.market.broker.bybit.connection :refer [send-msg-task!]]
   [quanta.market.util :refer [first-match]]))

(defn- sign
  [to-sign key-secret]
  (-> (crypto.mac/hash to-sign {:key key-secret :alg :hmac+sha256})
      (crypto.codecs/bytes->hex)))

(defn auth-msg [api-key api-secret]
  (let [millis (System/currentTimeMillis)
        expires (+ millis (* 1000 60 5)) ; 5 min
        to-sign (str "GET/realtime" expires)
        signature (sign to-sign api-secret)]
    {"op" "auth"
     "args" [api-key
             expires
             signature]}))

(def test-msg-auth-success
  {:reqId "0MczApsc" ; optional
   :op "auth"
   :retCode 0
   :retMsg "OK"
   :connId "cpv85t788smd5eps8ncg-2tfk"})

(def auth-error-example
  {:retCode 20001
   :retMsg "Repeat auth"
   :connId "cpv85t788smd5eps8ncg-2wqa"
   :op "auth"})

(defn auth-respose? [{:keys [op]}]
  (= op "auth"))

(defn authenticate! [conn {:keys [api-key api-secret]}]
  (m/sp
   (info "auth! api-key: " api-key)
   (m/? (send-msg-task! conn (auth-msg api-key api-secret)))
   (let [auth-result (m/? (first-match auth-respose? (:msg-in-flow conn)))]
     (info "auth result: " auth-result)
     auth-result)))





