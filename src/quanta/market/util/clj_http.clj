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
  " http-get using Aleph, which is modelled after clj-http.
    difference: we return a missionary task."
  ([url]
   (let [get-d (http/get url)]
     (deferred->task get-d)))
  ([url opts]
   (let [get-d (http/get url opts)]
     (deferred->task get-d))))

(defn http-get-body
  ([url]
   (m/sp
    (let [{:keys [body]} (m/? (http-get url))]
      (bs/to-string body))))
  ([url opts]
   (m/sp
    (let [{:keys [body]} (m/? (http-get url opts))]
      (bs/to-string body)))))

(defn parse-json [json]
  (j/read-value json j/keyword-keys-object-mapper))

(defn http-get-body-json
  ([url]
   (m/sp
    (let [body (m/? (http-get-body url))]
      (parse-json body))))
  ([url opts]
   (m/sp
    (let [body (m/? (http-get-body url opts))]
      (parse-json body)))))

(defn http-post
  " http-post using Aleph, which is modelled after clj-http.
    difference: we return a missionary task."
  ([url]
   (let [get-d (http/post url)]
     (deferred->task get-d)))
  ([url opts]
   (let [get-d (http/post url opts)]
     (deferred->task get-d))))
