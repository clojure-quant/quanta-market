(ns demo.test
    (:require
    [missionary.core :as m]
    [quanta.market.protocol :as p]
    [demo.tm :refer [tm]]))



(def account (-> tm :accounts :florian/test1 :opts))

(def conn (-> tm :accounts :florian/test1 :conn))

account
conn




(require '[quanta.market.broker.bybit.task.ping :refer [ping!]])

(m/? (ping! @conn))


(require '[quanta.market.broker.bybit.task.auth :refer [authenticate!]])

(m/?  (authenticate! @conn account))

