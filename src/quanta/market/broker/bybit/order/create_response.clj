(ns quanta.market.broker.bybit.order.create-response)

(def order-response-success-example
{:retCode 0
 :retMsg "OK"
 :connId "cpv85t788smd5eps8ncg-3bbo"
 :op "order.create"
 :header {:Timenow 1722550611004
          :X-Bapi-Limit-Status 19
          :X-Bapi-Limit-Reset-Timestamp "1722550611003"
          :Traceid "329a6c89ee3055c26d775c785b80d175"
          :X-Bapi-Limit 20}
 :reqId "A5PYbicF"
 :data {:orderLinkId "ZNMid1vM"
        :orderId 1743275308385396736}})

(def order-response-failed-example
  {:retCode 110007,
   :retMsg "ab not enough for new order",
   :connId "cpv85t788smd5eps8ncg-2tgm",
   :op "order.create",
   :header {:Timenow 1721152638764,
            :X-Bapi-Limit-Status 9,
            :X-Bapi-Limit-Reset-Timestamp 1721152638762,
            :Traceid "2bc167807ee719a474416104ae7e964b",
            :X-Bapi-Limit 10},
   :reqId "5bY1PVT-"
   :data {}})

(def order-response-failed-example2
  {:retCode 10005,
   :retMsg "Permission denied for current apikey",
   :connId "cpv86i6c0hvd5nkl25n0-2v79",
   :op "order.create",
   :reqId "XiMTlhQV"})

(def order-response-failed-example3
  {:op "order.create",
   :success false,
   :conn_id "cq1814tdaugt75sdcg8g-2ljtq",
   :ret_msg "Params Error"})

(defn parse-order-response [order {:keys [success retCode ret_msg retMsg] :as response}]
  (if (or success (= 0 retCode))
    {:msg/type :order/confirmed
     :order order}
    {:msg/type :order/rejected
     :order order
     :message (or retMsg ret_msg)
     :code retCode}))

(defn post-process-order [order]
  (partial parse-order-response order))