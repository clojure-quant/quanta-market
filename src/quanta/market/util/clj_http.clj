(ns quanta.market.util.clj-http
  (:require
   [missionary.core :as m]
   [taoensso.telemere :as tm]
   [clj-http.conn-mgr :as conn]
   [clj-http.client :as chttp]
   [jsonista.core :as j] ; json read/write
   ))

(def opts
  {:timeout 5 :threads 4 :insecure? false :default-per-route 5})

(def cm (conn/make-reusable-conn-manager opts))

(defn make-request [req-fn url opts]
  (binding [conn/*connection-manager* cm]
    (req-fn url opts)))

(defn req
  [req-fn url opts]
  (m/via m/blk
         (tm/log! (str "req  " url " opts: " opts))
         (make-request req-fn url opts)))

(defn http-head
  ([url]
   (req chttp/head url {}))
  ([url opts]
   (req chttp/head url opts)))

(defn http-get
  ([url]
   (req chttp/get url {}))
  ([url opts]
   (req chttp/get url opts)))