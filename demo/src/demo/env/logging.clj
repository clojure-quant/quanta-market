(ns demo.env.logging
  (:require
   [modular.log]))

(modular.log/timbre-config!
 {:min-level [[#{"org.apache.http.*"
                 "org.eclipse.aether.*"
                 "org.eclipse.jetty.*"
                 "modular.oauth2.*"
                 "modular.oauth2.token.refresh.*"
                 "modular.ws.*"
                 "webly.web.*"} :warn] ; webserver stuff - warn only
                                       ; [#{"modular.ws.*"} :debug]
              [#{"*"} :info]] ; default -> info
  :appenders {:default {:type :console-color}
              #_:rolling #_{:type :file-rolling
                            :path ".data/quanta.log"
                            :pattern :monthly}}})