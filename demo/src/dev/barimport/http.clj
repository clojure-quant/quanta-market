(ns dev.barimport.http
  (:require
   [taoensso.telemere :as tm]
   [missionary.core :as m]
   [quanta.market.util.aleph :as a]))

;; test success

((a/http-get "https://google.com/")
 #(println "success: " %)
 #(println "error: " %))

(-> (m/? (a/http-get "https://google.com/"))
    keys)
;; => (:request-time 
;       :aleph/keep-alive? 
;       :headers 
;       :status 
;       :connection-time 
;       :body 
;       :trace-redirects)

(-> (m/? (a/http-get "https://google.com/"))
    keys
    (tm/log!))

;; test error

((a/http-get "https://google.com888/")
 #(println "success: " %)
 #(println "error: " %))

(try
  (m/? (a/http-get "https://google.com888/"))
  (catch Exception ex
    (tm/log! (str "request error: " (ex-message ex)))
    (ex-message ex)))

;; get the data

(-> (m/? (a/http-get-body "https://google.com/"))
    (tm/log!))

(-> (m/? (a/http-get-body "https://jsonplaceholder.typicode.com/todos/1"))
     ;; => "{\n  \"userId\": 1,\n  \"id\": 1,\n  \"title\": \"delectus aut autem\",\n  \"completed\": false\n}"

    (tm/log!))

(def json
  "{\n  \"userId\": 1,\n  \"id\": 1,\n  \"title\": \"delectus aut autem\",\n  \"completed\": false\n}")

(a/parse-json json)
;; => {:completed false, :title "delectus aut autem", :id 1, :userId 1}

(m/? (a/http-get-body-json "https://jsonplaceholder.typicode.com/todos/1"))
;; => {:completed false, :title "delectus aut autem", :id 1, :userId 1}

;; with options

(-> (m/? (a/http-get "https://google.com/"
                     {:socket-timeout 900
                      :connection-timeout 900}))
    keys)
;; => (:request-time :aleph/keep-alive? :headers :status :connection-time :body :trace-redirects)

(m/? (a/http-get-body-json
      "https://jsonplaceholder.typicode.com/todos/1"
      {:socket-timeout 900
       :connection-timeout 900}))
;; => {:completed false, :title "delectus aut autem", :id 1, :userId 1}

