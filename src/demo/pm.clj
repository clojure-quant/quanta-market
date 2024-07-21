(ns demo.pm
  (:require
   [missionary.core :as m]
   [quanta.market.portfolio :as p :refer [portfolio-manager-start
                                    get-working-orders]]
   [demo.tm :refer [tm]]))



(def pm (p/portfolio-manager-start {:db nil
                                  :tm tm
                                  :alert-logfile ".data/alerts.txt"
                                  }))



(p/get-working-orders pm)
