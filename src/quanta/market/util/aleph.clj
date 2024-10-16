(ns quanta.market.util.aleph
  (:require
   [missionary.core :as m]
   [manifold.deferred :as d]
   [aleph.http :as http]
   [clj-commons.byte-streams :as bs]
   [jsonista.core :as j] ; json read/write
   ))

(defn deferred->task
  "Returns a missionary task completing with the result of given manifold-deferred."
  [df]
  ; see: https://github.com/leonoel/missionary/wiki/Task-interop#futures-promises
  (let [v (m/dfv)]
    (d/on-realized df
                   (fn [r]
                     ;(println "deferred success: " r)
                     (v (fn [] r))
                     ;(println "deferred success delivered!")
                     )
                   (fn [e]
                     ;(println "deferred error: " e)
                     (v (fn [] (throw e)))
                     ;(println "deferred error delivered!")
                     ))
    (m/absolve v)))

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