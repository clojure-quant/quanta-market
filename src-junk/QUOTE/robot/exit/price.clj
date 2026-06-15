(ns quanta.market.robot.exit.price
  (:require
   [missionary.core :as m]
   [quanta.market.robot.exit.rule :refer [get-exit-rule]]
   [quanta.market.robot.position.working :refer [working-position]]))

(defn trailing-return [{:keys [qm] :as env} position]
  (let [working-position (working-position qm position)]
    (m/latest :ret-prct working-position)))

(defn profit-trigger
  "returns a missionary task that emits :profit when target is met.
   if no profit-percent exit-rule is specified it returns nil
   if not task continues running."
  [env algo-opts position]
  (let [[_ target] (get-exit-rule algo-opts :profit-percent)]
    (when target
      (let [prct (trailing-return env position)
            rf (fn [_ tp]
                 (println "trailing profit " (:asset position) ": " tp " target: " target)
                 (when (and tp (> tp target))
                   (reduced :profit)))]
        (m/reduce rf nil prct)))))

(defn loss-trigger
  "returns a missionary task that emits :loss when target is met.
   if no :loss-percent exit-rule is specified it returns nil.
   if not task continues running."
  [env algo-opts position]
  (let [[_ target] (get-exit-rule algo-opts :loss-percent)]
    (when target
      (let [prct (trailing-return env position)
            rf (fn [_ tp]
                 (println "trailing loss " (:asset position) ": " tp " target: " target)
                 (when (and tp (< tp (- 0.0 target)))
                   (reduced :loss)))]
        (m/reduce rf nil prct)))))



