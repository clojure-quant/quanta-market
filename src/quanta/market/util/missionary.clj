(ns quanta.market.util.missionary
   (:require
    [missionary.core :as m]))

 (defn backoff [request delays]
   (if-some [[delay & delays] (seq delays)]
     (m/sp
      (try (m/? request)
           (catch Exception e
             (if (-> e ex-data :worth-retrying)
               (do (m/? (m/sleep delay))
                   (m/? (backoff request delays)))
               (throw e)))))
     request))

 (defn refill [{:keys [tokens last-ts rate capacity] :as st}]
   (let [t (System/currentTimeMillis)
         elapsed-ms (max 0 (- t last-ts))
         add (* rate (/ elapsed-ms 1000.0))
         new-tokens (min capacity (+ tokens add))]
     (assoc st :tokens new-tokens :last-ts t)))

 (defn bucket-delay-ms [st cost]
   (let [{:keys [tokens rate]} st
         deficit (max 0.0 (- cost tokens))]
     (long (Math/ceil (* 1000.0 (/ deficit rate))))))

 (defn consume [st cost]
   (update st :tokens (fn [x] (- x cost))))

;"Wrap a requests flow, delaying items until tokens available."
 (defn token-bucket-gate
   [requests {:keys [capacity rate]
              :or {capacity 20.0
                   rate 5.0}}]
   (m/ap (let [st (atom {:tokens capacity
                         :last-ts (System/currentTimeMillis)
                         :rate rate
                         :capacity capacity})]
           (println "st: " st)
           (let [job (m/?> 1 requests) ;(m/?< requests)
                 ]
             (println "got job: " job)
             (let [st1 (refill @st)
                   d   (bucket-delay-ms st1 1.0)]
               (when (pos? d)
                 (println "sleeping " d)
                 (m/? (m/sleep d)))
               (let [st2 (consume (refill st1) 1.0)]
                 (println "next")
                 (reset! st st2)
                 job))))))

(defn do-request [job]
  (m/sp
   (m/? (m/sleep 1000))
    ;; pretend rate-limit decreasing
   {:status 200
    :headers {"x-ratelimit-remaining"
              (str (max 0 (- 20 job)))
              "x-ratelimit-reset" "30"}}))

(def requests
  (m/seed (range 120) ;(map do-request (range 100))
          ))

(def responses
  (token-bucket-gate requests {:capacity 20 :rate 5}))

(m/? (m/reduce conj  (m/eduction
                      (take 50) responses)))

