(ns dev.barimport.kibot-http
  (:require
   [missionary.core :as m]
   [quanta.market.asset.db :as db]
   [ta.db.bars.protocol :refer [get-bars]]
   [quanta.market.barimport.kibot.http :as kibot-http]
   [dev.env :refer [secrets]]))

; test if EURUSD has kibot-http
(db/instrument-details "EUR/USD")
;; => {:kibot "EURUSD",
;;     :kibot-link-d
;;     "szsds2s4sls8gps7s4ses2sls51ppks21psvshgps6sls5sh1fpkscs8szsrs3scses2shsrgpp9pkse1psasksls9gpgdgegvgegngkpks4s8s2shs5stszs9gpsrszs4s91ppkses2szs5s2srszs2shgppapjpapjpapcpcp9pksrs4s5shsds2gppapkszs2s2szsds7sashs8s2gppapks5shsmscs9szs5seshseses4sls8gpp9pkscseshs5gps7slshs5s2s9shs7s8shs5g1smsaszs4s9p3sdslsapksvszsesesbsls5srgpa6arafaraeas7n72",
;;     :kibot-link-m
;;     "szsds2s4sls8gps7s4ses2sls51ppks21psvshgps6sls5sh1fpkscs8szsrs3scses2shsrgpp9pkse1psasksls9gpgdgegvgegngkpks4s8s2shs5stszs9gppapkses2szs5s2srszs2shgppapjpapjpapcpcp9pksrs4s5shsds2gppapkszs2s2szsds7sashs8s2gppapks5shsmscs9szs5seshseses4sls8gpp9pkscseshs5gps7slshs5s2s9shs7s8shs5g1smsaszs4s9p3sdslsapksvszsesesbsls5srgpa6arafaraeas7n72",
;;     :asset "EUR/USD",
;;     :category :forex,
;;     :name "Unknown: ",
;;     :exchange :us}

(def s (:kibot secrets))

s

(def k (kibot-http/create-import-kibot-http (:kibot secrets)))
k
(m/? (get-bars
      k
      {:asset "EUR/USD"
       :calendar [:forex :m]}
      :full))

; kibot-bars [527627 6]:
;|                                    :date |   :open |   :high |    :low |  :close | :volume |
;|------------------------------------------|--------:|--------:|--------:|--------:|--------:|
;| 2023-05-22T00:00-04:00[America/New_York] | 1.08209 | 1.08209 | 1.08192 | 1.08198 |     113 |
;| 2023-05-22T00:01-04:00[America/New_York] | 1.08198 | 1.08215 | 1.08192 | 1.08214 |      95 |
;| 2023-05-22T00:02-04:00[America/New_York] | 1.08213 | 1.08219 | 1.08211 | 1.08213 |     197 |
;| 2023-05-22T00:03-04:00[America/New_York] | 1.08214 | 1.08224 | 1.08214 | 1.08223 |      67 |
;| 2023-05-22T00:04-04:00[America/New_York] | 1.08224 | 1.08229 | 1.08222 | 1.08229 |      55 |
;| 2023-05-22T00:05-04:00[America/New_York] | 1.08228 | 1.08234 | 1.08220 | 1.08220 |      64 |
;| 2023-05-22T00:06-04:00[America/New_York] | 1.08220 | 1.08220 | 1.08216 | 1.08218 |      32 |
;| 2023-05-22T00:07-04:00[America/New_York] | 1.08218 | 1.08219 | 1.08215 | 1.08218 |      52 |
;| 2023-05-22T00:08-04:00[America/New_York] | 1.08218 | 1.08226 | 1.08217 | 1.08223 |      55 |
;| 2023-05-22T00:09-04:00[America/New_York] | 1.08224 | 1.08224 | 1.08209 | 1.08212 |      79 |
;|                                      ... |     ... |     ... |     ... |     ... |     ... |
;| 2024-10-21T12:01-04:00[America/New_York] | 1.08209 | 1.08212 | 1.08201 | 1.08210 |     125 |
;| 2024-10-21T12:02-04:00[America/New_York] | 1.08209 | 1.08212 | 1.08204 | 1.08204 |     101 |
;| 2024-10-21T12:03-04:00[America/New_York] | 1.08203 | 1.08204 | 1.08200 | 1.08204 |      49 |
;| 2024-10-21T12:04-04:00[America/New_York] | 1.08203 | 1.08208 | 1.08198 | 1.08208 |      79 |
;| 2024-10-21T12:05-04:00[America/New_York] | 1.08207 | 1.08216 | 1.08204 | 1.08214 |     103 |
;| 2024-10-21T12:06-04:00[America/New_York] | 1.08213 | 1.08229 | 1.08213 | 1.08227 |      68 |
;| 2024-10-21T12:07-04:00[America/New_York] | 1.08227 | 1.08227 | 1.08213 | 1.08215 |     133 |
;| 2024-10-21T12:08-04:00[America/New_York] | 1.08215 | 1.08218 | 1.08209 | 1.08210 |     167 |
;| 2024-10-21T12:09-04:00[America/New_York] | 1.08210 | 1.08215 | 1.08207 | 1.08214 |     100 |
;| 2024-10-21T12:10-04:00[America/New_York] | 1.08215 | 1.08220 | 1.08209 | 1.08210 |     116 |
;| 2024-10-21T12:11-04:00[America/New_York] | 1.08211 | 1.08214 | 1.08208 | 1.08213 |      59 |



