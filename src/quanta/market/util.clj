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

(defn stream-sender []
  (let [!-a (atom nil)]
    {:flow (msg-flow !-a)
     :send (fn [v]
             (when-let [! @!-a]
               (! v)))}))



(comment
  (m/?
   (first-match #(> % 3)
                (m/seed [1 2 3 4 5 6])))

  (m/? (m/reduce println nil
                 (current-value (m/seed [1 2 3]))))

  (m/? (current-value-task (m/seed [1 2 3])))

; 
  )




