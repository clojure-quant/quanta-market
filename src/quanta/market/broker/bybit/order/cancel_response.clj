(ns quanta.market.broker.bybit.order.cancel-response)

(def order-cancel-failed-example
  {:retCode 10005,
   :retMsg "Permission denied for current apikey",
   :connId "cpv86i6c0hvd5nkl25n0-2v7a",
   :op "order.cancel",
   :reqId "XMrZJoEs"})

(def order-cancel-failed-example2
  {:retCode 110001,
   :retMsg "order not exists or too late to cancel",
   :connId "cpv86i6c0hvd5nkl25n0-2v7a",
   :op "order.cancel",
   :header
   {:Timenow "1721243099025",
    :X-Bapi-Limit-Status "9",
    :X-Bapi-Limit-Reset-Timestamp "1721243099024",
    :Traceid "0313d8af73592c2034d873ef1f6479ac",
    :X-Bapi-Limit "10"},
   :reqId "-Cquvtz7",
   :data {}})