(ns dev.rest.kibot-http
  (:require
   [taoensso.telemere :as tm]
   [clojure.string :as str]
   [tick.core :as t]
   [missionary.core :as m]
   [quanta.market.barimport.kibot.raw :as kb]
   ;[quanta.market.adapter.bybit.normalize-request :refer [bybit-bar-params]]
   ))

(def header  "attachment; filename=ZARUSD.txt")
(kb/extract-asset header)

;(def url "http://api.kibot.com?action=download&link=fifzfgf9fjf8snf5f9fbfgfjfa1nnkfg1nfvf7snf4fjfaf71mnkf2f8fifrf3f2fbfgf7frsnn6nkfb1nfhfkfjf6snfmsusvsbslsknkf9f8fgf7faftfif6snnhnkfbfgfifafgfrfifgf7snnhncnhncnhn2n2n6nkfrf9faf7fzfgsnnhnkfifgfgfifzf5fhf7f8fgsnnhnkfaf7fdf2f6fifafbf7fbfbf9fjf8snn6nkf2fbf7fasnf5fjf7fafgf6f7f5f8f7fas1fdfhfif9f6n3fzfjfhnkfvfifbfbfefjfafrsnn8ngn8fhn8f4f5fdf51za4afaraeas7n72")

(def url "http://api.kibot.com/?action=download&link=fifzfgf9fjf8snf5f9fbfgfjfa1nnkfg1nfvf7snf4fjfaf71mnkf2f8fifrf3f2fbfgf7frsnn6nkfb1nfhfkfjf6snfmsusvsbslsknkf9f8fgf7faftfif6snnhnkfbfgfifafgfrfifgf7snnhncnhncnhn2n2n6nkfrf9faf7fzfgsnnhnkfifgfgfifzf5fhf7f8fgsnnhnkfaf7fdf2f6fifafbf7fbfbf9fjf8snn6nkf2fbf7fasnf5fjf7fafgf6f7f5f8f7fas1fdfhfif9f6n3fzfjfhnkfvfifbfbfefjfafrsnn8ngn8fhn8f4f5fdf51za4afaraeas7n72")

(defn save-csv-data [asset data]
  (spit (str ".data/kibot/" asset ".txt")
        data))

(defn load-csv-data [asset]
  (slurp (str ".data/kibot/" asset ".txt")))

(let [{:keys [asset data]} (m/? (kb/download-link url))]
  (tm/log! (str "bars rcvd for asset: " asset " size:" (count data)))
  (save-csv-data asset data)
  :done)

; 1 sec until link download finished!
; asset: ZARUSD size: 34.961.624
; 15 secs till data downloaded.

; time curl --compressed -i  "http://api.kibot.com/?action=download&link=fifzfgf9fjf8snf5f9fbfgfjfa1nnkfg1nfvf7snf4fjfaf71mnkf2f8fifrf3f2fbfgf7frsnn6nkfb1nfhfkfjf6snfmsusvsbslsknkf9f8fgf7faftfif6snnhnkfbfgfifafgfrfifgf7snnhncnhncnhn2n2n6nkfrf9faf7fzfgsnnhnkfifgfgfifzf5fhf7f8fgsnnhnkfaf7fdf2f6fifafbf7fbfbf9fjf8snn6nkf2fbf7fasnf5fjf7fafgf6f7f5f8f7fas1fdfhfif9f6n3fzfjfhnkfvfifbfbfefjfafrsnn8ngn8fhn8f4f5fdf51za4afaraeas7n72" >> bongo3.txt
; time curl --compressed -i  "http://api.kibot.com?action=download&link=fifzfgf9fjf8snf5f9fbfgfjfa1nnkfg1nfvf7snf4fjfaf71mnkf2f8fifrf3f2fbfgf7frsnn6nkfb1nfhfkfjf6snfmsusvsbslsknkf9f8fgf7faftfif6snnhnkfbfgfifafgfrfifgf7snnhncnhncnhn2n2n6nkfrf9faf7fzfgsnnhnkfifgfgfifzf5fhf7f8fgsnnhnkfaf7fdf2f6fifafbf7fbfbf9fjf8snn6nkf2fbf7fasnf5fjf7fafgf6f7f5f8f7fas1fdfhfif9f6n3fzfjfhnkfvfifbfbfefjfafrsnn8ngn8fhn8f4f5fdf51za4afaraeas7n72" >> bongo3.txt
;; curl compressed takes 10-20 secs

;(http/head url)



