(ns demo.dev.flow-test
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]))

(def !-a (atom nil))

(def msg-flow
  (m/stream
   (m/observe
    (fn [!]
      (info "creating msg-flow reader..")
      (reset! !-a !)
      (fn []
        (info "removing msg-flow reader..")
        (reset! !-a nil))))))

(defn send! [v]
  (info "sending: " v)
  (@!-a v))

(defn start []
  (let [a (m/reduce (fn [r v]
                      (println "A: " v)) nil msg-flow)
        b (m/reduce (fn [r v]
                      (println "B: " v)) nil msg-flow)]
    [(a #(println "a success " %) #(println "a err " %))
     (b #(println "b success " %) #(println "b err " %))]))

(def x (start))

x

(send! 1)

(def msg-flow (m/seed [1 2 3]))

(send! 1)
