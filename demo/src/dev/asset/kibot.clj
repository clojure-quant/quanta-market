(ns dev.asset.kibot
  (:require
   [missionary.core :as m]
   [clj-commons.byte-streams :as bs]
   [quanta.market.util.aleph :as a]
   [quanta.market.barimport.kibot.http :refer [download-link download-link-info download-link-info2 remove-prefix]]
   [quanta.market.barimport.kibot.raw :as raw]
   [quanta.market.barimport.kibot.asset.scraper :refer [login download-calendars download-calendar]]
   [quanta.market.barimport.kibot.asset.db :as db]
   ;[quanta.market.barimport.kibot.asset.assets :refer [assets-for]]
   [dev.env :refer [secrets]]))

(def s (:kibot secrets))

(m/? (download-calendar s [:etf :d]))
(m/? (download-calendar s [:forex :d]))
(m/? (download-calendar s [:forex :m]))

(m/? (download-calendars s))

(m/?
 (db/import-calendar s [:forex :m]))

(def link
  "http://api.kibot.com/?action=download&link=pupgpfp9pbps13p4p9pnpfpbp8v33hpfv3pkp513pdpbp8p5vz3hprpspup2p6prpnpfp5p2133m3hpnv3paphpbpm131i1g1hpz1i1k3hp9pspfp5p8pjpupm133a3hpnpfpup8pfp2pupfp5133a3c3a3c3a3r3r3m3hp2p9p8p5pgpf133a3hpupfpfpupgp4pap5pspf133a3hp8p5p7prpmpup8pnp5pnpnp9pbps133m3hprpnp5p813p4pbp5p8pfpmp5p4psp5p81vp7papup9pm36pgpbpa3hpkpupnpnptpbp8p213a4a6as1za4af7n72")

(remove-prefix link)

(try
  (m/? (raw/login s))
  ;(m/? (download-link-info link))
  (m/? (download-link-info (remove-prefix link)))
  ;(m/? (download-link-info2 link))
  (catch Exception ex
    (println "ex: " (ex-message ex))
    (println "ex full: " ex)))

(try
  (m/? (raw/login s))
  (m/? (download-link  (remove-prefix link)))
  (catch Exception ex
    (println "ex: " (ex-message ex))
    (println "ex full: " ex)))

(assets-for ".data/" "etf")
