(require '[babashka.curl :as curl])
(require '[clojure.java.io :as io]) ;; optional
(require '[cheshire.core :as json]) ;; optional


;(:body 
 (curl/get "ftp://ftp.kibot.com/Updates/" 
          {:basic-auth ["" 
                        ""]})
 
 ;)
;; => "{\"authenticated\":true}"