(ns quanta.market.util
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m])
  (:import [missionary Cancelled]))

(defn first-match [predicate flow]
  (m/reduce (fn [_r v]
              (debug "first-match check: " v)
              (when (predicate v)
                (debug "success! returning: " v)
                (reduced v)))
            nil
            flow))

(defn next-value [flow]
  (first-match #(not (nil? %)) flow))

(defn always [flow]
  (m/reduce (fn [_r v]
              v)
            nil
            flow))

(defn current-value [flow]
  ; flows dont implement deref
  (m/eduction (take 1) flow))

(defn current-value-task [flow]
  (m/reduce (fn [_r v]
              v) nil
            (current-value flow)))

(defn msg-flow [!-a]
  ; without the stream the last subscriber gets all messages
  (m/stream
   (m/observe
    (fn [!]
      (reset! !-a !)
      (fn []
        (reset! !-a nil))))))

(defn flow-sender
  "returns {:flow f
            :send s}
    (s v) pushes v to f."
  []
  (let [!-a (atom nil)]
    {:flow (msg-flow !-a)
     :send (fn [v]
             (when-let [! @!-a]
               (! v)))}))


(defn mix
  "Return a flow which is mixed by flows"
  ; will generate (count flows) processes, 
  ; so each mixed flow has its own process
  [& flows]
  (m/ap (m/?> (m/?> (count flows) (m/seed flows)))))


  (defn cont [flow]
  (->> flow
       (m/reductions (fn [r v]
                       (if v v r)) nil)
       (m/relieve {})))


(defn start-printing [flow label-str]
  (let [print-task (m/reduce (fn [r v]
                               (println label-str " " v)
                               nil)
                             nil flow)]
    (print-task
     #(println "flow-printer completed: " %)
     #(println "flow-printer crashed: " %))))

(defn start-logging [file-name flow]
  (let [print-task (m/reduce (fn [r v]
                               (let [s (with-out-str (println v))]
                                 (spit file-name s :append true))
                               nil)
                             nil flow)]
    (print-task
     #(println "flow-logger completed: " %)
     #(println "flow-logger crashed: " %))))

(defn split-seq-flow [s]
  (m/ap
   (loop [s s]
     (m/amb
      (first s)
      (let [s (rest s)]
        (when (seq s)
          (recur s)))))))

(defn start! [task]
  (task
    #(println "task completed: " %)
    #(println "task crashed: " %)))

(comment
  (m/?
   (first-match #(> % 3)
                (m/seed [1 2 3 4 5 6])))

  (m/? (m/reduce println nil
                 (current-value (m/seed [1 2 3]))))

  (m/? (current-value-task (m/seed [1 2 3])))


  (m/?
   (m/reduce println nil
             (mix (m/seed [1 2 3 4 5 6 7 8]) (m/seed [:a :b :c]))))


; 
  )




