(ns demo.pm
  (:require
   [missionary.core :as m]
   [quanta.market.portfolio :refer [portfolio-manager-start
                                    get-working-orders]]
   [demo.tm :refer [tm]]))



(def pm (portfolio-manager-start {:db nil
                                  :tm tm
                                  :alert-logfile ".data/alerts.txt"
                                  }))


(get-working-orders pm)
