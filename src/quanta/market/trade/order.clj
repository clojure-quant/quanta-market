(ns quanta.market.trade.order
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [tick.core :as t]
   [missionary.core :as m]))

(defn filled? [{:keys [order-open order-status] :as working-order}]
  (>= (:fill-qty order-status) (:qty order-open)))

(defn extract-trade [{:keys [open-order order-status] :as working-order}
                     {:keys [fill-qty fill-value] :as broker-order-status}]
  (if (and fill-qty fill-value)
    (let [_ (info "extracting trade: " broker-order-status)
          cur-fill-qty (:fill-qty order-status)
          cur-fill-value (:fill-value order-status)
          open-qty (- (:qty open-order) cur-fill-qty)
          broker-trade-qty (- fill-qty cur-fill-qty)
          broker-trade-value (- fill-value cur-fill-value)
          _ (info "broker-qty-trade: " broker-trade-qty)
          ]
      (if (> broker-trade-qty 0)
        (if (<= broker-trade-qty open-qty)
          {:new-trade {:qty broker-trade-qty :value broker-trade-value}}
          {:new-trade {:qty open-qty :value (/ (* broker-trade-value open-qty) broker-trade-qty)}
           :alert-trade {:text "received order-status that has fill BIGGER than initial order qty."
                         :data broker-order-status}})
        {}))
    {}))


(defn process-order-status [{:keys [open-order order-status] :as working-order}
                            {:keys [status close-reason] :as broker-order-status}]
  (let [; status prior to applying the broker-order-status
        cur-fill-qty (:fill-qty order-status)
        cur-fill-value (:fill-value order-status)
        ; extract new-trade 
        {:keys [new-trade alert-trade]} (extract-trade working-order broker-order-status)
        ; new values
        new-fill-status (if new-trade
                          {:fill-qty (+ (:qty new-trade) cur-fill-qty)
                           :fill-value (+ (:value new-trade) cur-fill-value)}
                          {})
        filled? (when (:fill-qty new-fill-status)
                  (>= (:fill-qty new-fill-status) (:qty open-order)))
        new-close-status  (cond
                            filled? {:status :closed
                                     :close-reason "filled"
                                     :close-date (t/inst)}
                            (= status :closed) {:status :closed
                                                :close-reason close-reason
                                                :close-date (t/inst)}
                            :else {})
        close-trans (if (= :closed (:status new-close-status))
                      {:order/close new-close-status}
                      {})
        trade-transaction (if new-trade {:trade new-trade} {})
        ]
    {:open-order open-order
     :order-status (merge order-status
                          new-fill-status
                          new-close-status)
     :transactions (merge trade-transaction close-trans)}))

(defn create-working-order [order]
  {:open-order order
   :order-status {:status :open
                  :open-date (t/inst)
                  :fill-qty 0.0
                  :fill-value 0.0}
   :transactions {:order/created order}})

(defn working-order-flow
  "consumes order-flow (at this point it should be filtered by order-id
   returns working-order order-flow.
   this flow can be used to update the order-status of a working-order ui,
   and it can be used to extract events like :alert :order/open :order/close"
  [order-orderupdate-flow]
  (m/reductions (fn [{:keys [open-order order-status] :as working-order}
                     {:keys [order broker-order-status] :as msg}]
                  (cond
                    ; new order
                    (and (nil? open-order) order)
                    (create-working-order order)
                    ; order update  
                    (and open-order broker-order-status)
                    (let [new-working-order (process-order-status working-order broker-order-status)
                          order-closed? (get-in new-working-order [:transactions :order/close])]
                      (if order-closed?
                        (reduced new-working-order)
                        new-working-order))
                    :else
                    (assoc working-order
                           :transactions {:alert {:text "logic error"
                                                  :data {:working-order working-order
                                                         :msg msg}}})))
                {}
                order-orderupdate-flow))


(defn order-change-flow [order-orderupdate-flow]
  (m/ap
   (let [[order-id >x] (m/?> ##Inf (m/group-by :order-id order-orderupdate-flow))
         _ (info "creating flow for order-id: " order-id)
         working-order (m/?> 1 (working-order-flow >x))]
     [order-id working-order])))


(defn working-orders-flow [order-change-flow]
  (m/ap
   (let [orders (atom {})
         [order-id {:keys [order-status open-order transactions]}] (m/?> 1 order-change-flow)
         order-open (:order/open transactions)
         order-close (:order/close transactions)]
     (cond
       order-open (m/amb)
       order-close (swap! orders dissoc order-id)
       :else
       (swap! orders assoc order-id {:order-status order-status
                                     :open-order open-order})))))

(defn current-working-orders [order-orderupdate-flow]
  (let [order-changes (order-change-flow order-orderupdate-flow)
        order-status (working-orders-flow order-changes)
        last-status (m/? (m/reduce (fn [_r v] v) {} order-status))]
    (map (fn [[order-id working-order]]
           ;  working-order
           (assoc working-order :order-id order-id)) last-status)))

