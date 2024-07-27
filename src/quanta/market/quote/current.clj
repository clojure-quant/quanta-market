(ns quanta.market.quote.current
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [missionary.core :as m]
   [quanta.market.protocol :as p]
   [quanta.market.util :as util])
  (:import [missionary Cancelled]))

(defonce subscriptions (atom {}))

(def lock (m/sem))

(defn subscribing-unsubscribing-quote-flow [qm sub]
  (info "get-quote will start a new subscription..")
  (let [q (p/last-trade-flow qm sub)]
    (m/? (p/subscribe-last-trade! qm sub))
    (util/cont
     (m/ap (try
             (m/amb (m/?> q))
             (catch Cancelled _
               (do  (info "get-quote will stop an existing subscription..")
                    (m/? (p/unsubscribe-last-trade! qm sub))
                    (info "get-quote has unsubscribed. now removing from atom..")
                    (m/holding lock
                               (swap! subscriptions dissoc sub)))))))))

(defn get-quote [qm sub]
  (or (get @subscriptions sub)
      (m/holding lock
                 (let [qs (subscribing-unsubscribing-quote-flow qm sub)]
                   (swap! subscriptions assoc sub qs)
                   qs))))

(comment
  @subscriptions
  (reset! subscriptions {})

 ; 
  )



2024-07-27T18:08:01.815Z nuc12 INFO [quanta.market.quote.current:21] - get-quote will stop an existing subscription..
task crashed:  #error {
 :cause No implementation of method: :unsubscribe-last-trade! 
                       of protocol: #'quanta.market.protocol/quotefeed found for 
                       class: quanta.market.quote.quote-manager
 :via
 [{:type java.lang.IllegalArgumentException
   :message No implementation of method: :unsubscribe-last-trade! of protocol: #'quanta.market.protocol/quotefeed found for class: quanta.market.quote.quote-manager
   :at [clojure.core$_cache_protocol_fn invokeStatic core_deftype.clj 584]}]
 :trace
 [[clojure.core$_cache_protocol_fn invokeStatic core_deftype.clj 584]
  [clojure.core$_cache_protocol_fn invoke core_deftype.clj 576]
  [quanta.market.protocol$eval41159$fn__41173$G__41146__41180 invoke NO_SOURCE_FILE 13]
  [quanta.market.quote.current$subscribing_unsubscribing_quote_flow$cr19046_block_5__19073 invoke current.clj 22]
  [cloroutine.impl$coroutine$fn__13132 invoke impl.cljc 61]
  [missionary.impl.Ambiguous ready Ambiguous.java 351]
  [missionary.impl.Ambiguous$1 invoke Ambiguous.java 93]
  [missionary.impl.Ambiguous backtrack Ambiguous.java 106]
  [missionary.impl.Ambiguous branch Ambiguous.java 143]