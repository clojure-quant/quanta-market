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
;; => #'dev.barimport.kibot-http/s

s
;; => {:user "hoertlehner@gmail.com", :password "282m2fhgh"}

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

(m/? (get-bars
      k
      {:asset "EUR/USD"
       :calendar [:forex :d]}
      :full))
;; => kibot-bars [7099 6]:
;;    
;;    |                                    :date |   :open |   :high |    :low |  :close | :volume |
;;    |------------------------------------------|--------:|--------:|--------:|--------:|--------:|
;;    | 1997-01-14T16:30-05:00[America/New_York] | 1.24560 | 1.24560 | 1.24140 | 1.24140 |       0 |
;;    | 1997-01-15T16:30-05:00[America/New_York] | 1.24140 | 1.24550 | 1.24140 | 1.24550 |       0 |
;;    | 1997-01-16T16:30-05:00[America/New_York] | 1.24550 | 1.24550 | 1.24220 | 1.24220 |       0 |
;;    | 1997-01-17T16:30-05:00[America/New_York] | 1.24220 | 1.24220 | 1.22820 | 1.22820 |       0 |
;;    | 1997-01-22T16:30-05:00[America/New_York] | 1.19920 | 1.20420 | 1.19470 | 1.19630 |       0 |
;;    | 1997-01-23T16:30-05:00[America/New_York] | 1.19630 | 1.19630 | 1.18360 | 1.18640 |       0 |
;;    | 1997-01-24T16:30-05:00[America/New_York] | 1.18640 | 1.19010 | 1.17960 | 1.18650 |       0 |
;;    | 1997-01-28T16:30-05:00[America/New_York] | 1.18550 | 1.18720 | 1.17580 | 1.17580 |       0 |
;;    | 1997-01-29T16:30-05:00[America/New_York] | 1.17580 | 1.18210 | 1.16990 | 1.17350 |       0 |
;;    | 1997-01-30T16:30-05:00[America/New_York] | 1.17350 | 1.18460 | 1.16980 | 1.17800 |       0 |
;;    |                                      ... |     ... |     ... |     ... |     ... |     ... |
;;    | 2024-10-04T16:30-04:00[America/New_York] | 1.10289 | 1.10396 | 1.09512 | 1.09716 |  234351 |
;;    | 2024-10-07T16:30-04:00[America/New_York] | 1.09668 | 1.09868 | 1.09542 | 1.09742 |  224858 |
;;    | 2024-10-08T16:30-04:00[America/New_York] | 1.09700 | 1.09972 | 1.09609 | 1.09800 |  208460 |
;;    | 2024-10-09T16:30-04:00[America/New_York] | 1.09759 | 1.09810 | 1.09361 | 1.09391 |  193187 |
;;    | 2024-10-10T16:30-04:00[America/New_York] | 1.09378 | 1.09549 | 1.08999 | 1.09365 |  249857 |
;;    | 2024-10-11T16:30-04:00[America/New_York] | 1.09336 | 1.09537 | 1.09260 | 1.09358 |  167467 |
;;    | 2024-10-14T16:30-04:00[America/New_York] | 1.09284 | 1.09367 | 1.08881 | 1.09085 |  149732 |
;;    | 2024-10-15T16:30-04:00[America/New_York] | 1.09086 | 1.09168 | 1.08818 | 1.08907 |  173326 |
;;    | 2024-10-16T16:30-04:00[America/New_York] | 1.08887 | 1.09014 | 1.08531 | 1.08619 |  166864 |
;;    | 2024-10-17T16:30-04:00[America/New_York] | 1.08618 | 1.08735 | 1.08110 | 1.08311 |  203582 |
;;    | 2024-10-18T16:30-04:00[America/New_York] | 1.08311 | 1.08694 | 1.08252 | 1.08649 |  154070 |

(db/instrument-details "NG0")
;; => {:kibot "NG",
;;     :kibot-link-d
;;     "m2m6memcmkmd13mlmcmrmemkmfz334mez3mami13m8mtmemtmfmimr34mtmdm2m7mumtmrmemim7133j34mrz3mgm4mkmj131j1i34mcmdmemimfmhm2mj13m7m2mcmjz334mrmem2mfmem7m2memi133g3b3g3b3g3t3t3j34m7mcmfmim6me133g34m2memem2m6mlmgmimdme133g34mfmim9mtmjm2mfmrmimrmrmcmkmd133j34mtmrmimf13mlmkmimfmemjmimlmdmimf1zm9mgm2mcmj3um6mkmg34mam2mrmrmvmkmfm713avap7v7f",
;;     :kibot-link-m
;;     "m2m6memcmkmd13mlmcmrmemkmfz334mez3mami13m8mtmemtmfmimr34mtmdm2m7mumtmrmemim7133j34mrz3mgm4mkmj131j1i34mcmdmemimfmhm2mj133g34mrmem2mfmem7m2memi133g3b3g3b3g3t3t3j34m7mcmfmim6me133g34m2memem2m6mlmgmimdme133g34mfmim9mtmjm2mfmrmimrmrmcmkmd133j34mtmrmimf13mlmkmimfmemjmimlmdmimf1zm9mgm2mcmj3um6mkmg34mam2mrmrmvmkmfm713avap7v7f",
;;     :asset "NG0",
;;     :future "NG",
;;     :category :future,
;;     :name "Unknown: ",
;;     :exchange :us}

(m/? (get-bars
      k
      {:asset "NG0"
       :calendar [:us :m]}
      :full))
;; => kibot-bars [441762 6]:
;;    
;;    |                                    :date | :open | :high |  :low | :close | :volume |
;;    |------------------------------------------|------:|------:|------:|-------:|--------:|
;;    | 2023-05-22T00:00-04:00[America/New_York] | 2.531 | 2.531 | 2.529 |  2.530 |       5 |
;;    | 2023-05-22T00:02-04:00[America/New_York] | 2.529 | 2.529 | 2.529 |  2.529 |       1 |
;;    | 2023-05-22T00:03-04:00[America/New_York] | 2.530 | 2.531 | 2.530 |  2.531 |      20 |
;;    | 2023-05-22T00:04-04:00[America/New_York] | 2.531 | 2.532 | 2.531 |  2.532 |       2 |
;;    | 2023-05-22T00:05-04:00[America/New_York] | 2.532 | 2.532 | 2.532 |  2.532 |       1 |
;;    | 2023-05-22T00:07-04:00[America/New_York] | 2.531 | 2.531 | 2.531 |  2.531 |       3 |
;;    | 2023-05-22T00:08-04:00[America/New_York] | 2.530 | 2.530 | 2.530 |  2.530 |       3 |
;;    | 2023-05-22T00:11-04:00[America/New_York] | 2.530 | 2.530 | 2.529 |  2.529 |       5 |
;;    | 2023-05-22T00:14-04:00[America/New_York] | 2.528 | 2.529 | 2.528 |  2.529 |       2 |
;;    | 2023-05-22T00:15-04:00[America/New_York] | 2.527 | 2.528 | 2.527 |  2.528 |       4 |
;;    |                                      ... |   ... |   ... |   ... |    ... |     ... |
;;    | 2024-10-21T03:19-04:00[America/New_York] | 2.269 | 2.269 | 2.268 |  2.268 |       7 |
;;    | 2024-10-21T03:20-04:00[America/New_York] | 2.267 | 2.268 | 2.267 |  2.268 |       4 |
;;    | 2024-10-21T03:21-04:00[America/New_York] | 2.267 | 2.267 | 2.266 |  2.266 |       8 |
;;    | 2024-10-21T03:22-04:00[America/New_York] | 2.266 | 2.267 | 2.266 |  2.267 |       4 |
;;    | 2024-10-21T03:23-04:00[America/New_York] | 2.266 | 2.267 | 2.265 |  2.267 |      18 |
;;    | 2024-10-21T03:24-04:00[America/New_York] | 2.267 | 2.267 | 2.267 |  2.267 |       2 |
;;    | 2024-10-21T03:25-04:00[America/New_York] | 2.266 | 2.266 | 2.266 |  2.266 |       4 |
;;    | 2024-10-21T03:26-04:00[America/New_York] | 2.267 | 2.267 | 2.267 |  2.267 |       3 |
;;    | 2024-10-21T03:27-04:00[America/New_York] | 2.268 | 2.269 | 2.268 |  2.268 |      11 |
;;    | 2024-10-21T03:28-04:00[America/New_York] | 2.267 | 2.267 | 2.267 |  2.267 |       4 |
;;    | 2024-10-21T03:29-04:00[America/New_York] | 2.267 | 2.268 | 2.267 |  2.267 |      12 |

(m/? (get-bars
      k
      {:asset "NG0"
       :calendar [:us :d]}
      :full))
;; => kibot-bars [7220 6]:
;;    
;;    |                                    :date | :open | :high |  :low | :close | :volume |
;;    |------------------------------------------|------:|------:|------:|-------:|--------:|
;;    | 1995-12-22T16:30-05:00[America/New_York] | 2.611 | 2.630 | 2.340 |  2.368 |   25918 |
;;    | 1995-12-26T16:30-05:00[America/New_York] | 2.350 | 2.480 | 2.340 |  2.468 |    6527 |
;;    | 1995-12-27T16:30-05:00[America/New_York] | 2.470 | 2.990 | 2.461 |  2.868 |   26858 |
;;    | 1995-12-28T16:30-05:00[America/New_York] | 2.885 | 2.930 | 2.410 |  2.509 |   22306 |
;;    | 1995-12-29T16:30-05:00[America/New_York] | 2.500 | 2.690 | 2.488 |  2.619 |   10831 |
;;    | 1996-01-02T16:30-05:00[America/New_York] | 2.750 | 2.900 | 2.750 |  2.859 |   13970 |
;;    | 1996-01-03T16:30-05:00[America/New_York] | 2.880 | 3.100 | 2.860 |  2.986 |   18517 |
;;    | 1996-01-04T16:30-05:00[America/New_York] | 2.980 | 3.029 | 2.870 |  2.964 |   15087 |
;;    | 1996-01-05T16:30-05:00[America/New_York] | 2.950 | 2.970 | 2.880 |  2.916 |   12008 |
;;    | 1996-01-09T16:30-05:00[America/New_York] | 3.080 | 3.099 | 2.920 |  2.928 |   11460 |
;;    |                                      ... |   ... |   ... |   ... |    ... |     ... |
;;    | 2024-10-04T16:30-04:00[America/New_York] | 2.971 | 3.019 | 2.831 |  2.854 |  162159 |
;;    | 2024-10-07T16:30-04:00[America/New_York] | 2.827 | 2.839 | 2.705 |  2.746 |  172516 |
;;    | 2024-10-08T16:30-04:00[America/New_York] | 2.733 | 2.781 | 2.699 |  2.733 |  162513 |
;;    | 2024-10-09T16:30-04:00[America/New_York] | 2.709 | 2.724 | 2.632 |  2.660 |  190644 |
;;    | 2024-10-10T16:30-04:00[America/New_York] | 2.647 | 2.700 | 2.588 |  2.675 |  188461 |
;;    | 2024-10-11T16:30-04:00[America/New_York] | 2.687 | 2.727 | 2.615 |  2.632 |  121746 |
;;    | 2024-10-14T16:30-04:00[America/New_York] | 2.624 | 2.625 | 2.476 |  2.494 |  139692 |
;;    | 2024-10-15T16:30-04:00[America/New_York] | 2.482 | 2.550 | 2.445 |  2.498 |  156441 |
;;    | 2024-10-16T16:30-04:00[America/New_York] | 2.488 | 2.497 | 2.358 |  2.367 |  173482 |
;;    | 2024-10-17T16:30-04:00[America/New_York] | 2.377 | 2.417 | 2.338 |  2.347 |  135847 |
;;    | 2024-10-18T16:30-04:00[America/New_York] | 2.349 | 2.370 | 2.248 |  2.258 |  144208 |

(m/? (get-bars
      k
      {:asset "QQQ"
       :calendar [:us :m]}
      :full))
;; => kibot-bars [297521 6]:
;;    
;;    |                                    :date |    :open |    :high |     :low |  :close | :volume |
;;    |------------------------------------------|---------:|---------:|---------:|--------:|--------:|
;;    | 2023-05-22T04:00-04:00[America/New_York] | 333.3400 | 333.7300 | 333.3400 | 333.610 |    6851 |
;;    | 2023-05-22T04:01-04:00[America/New_York] | 333.5800 | 333.5800 | 333.5800 | 333.580 |     448 |
;;    | 2023-05-22T04:02-04:00[America/New_York] | 333.5400 | 333.5400 | 333.5200 | 333.520 |    2847 |
;;    | 2023-05-22T04:03-04:00[America/New_York] | 333.5100 | 333.5100 | 333.5100 | 333.510 |     202 |
;;    | 2023-05-22T04:05-04:00[America/New_York] | 333.4000 | 333.4200 | 333.3900 | 333.420 |    8032 |
;;    | 2023-05-22T04:06-04:00[America/New_York] | 333.4900 | 333.4900 | 333.4900 | 333.490 |     807 |
;;    | 2023-05-22T04:13-04:00[America/New_York] | 333.4200 | 333.4200 | 333.4200 | 333.420 |     101 |
;;    | 2023-05-22T04:14-04:00[America/New_York] | 333.4100 | 333.4200 | 333.4100 | 333.420 |     202 |
;;    | 2023-05-22T04:15-04:00[America/New_York] | 333.4600 | 333.4600 | 333.4300 | 333.430 |     969 |
;;    | 2023-05-22T04:17-04:00[America/New_York] | 333.4100 | 333.4500 | 333.4100 | 333.450 |     654 |
;;    |                                      ... |      ... |      ... |      ... |     ... |     ... |
;;    | 2024-10-21T13:44-04:00[America/New_York] | 494.4900 | 494.5000 | 494.4200 | 494.445 |   25308 |
;;    | 2024-10-21T13:45-04:00[America/New_York] | 494.4600 | 494.6600 | 494.4100 | 494.610 |   33873 |
;;    | 2024-10-21T13:46-04:00[America/New_York] | 494.6099 | 494.6475 | 494.5000 | 494.580 |   27801 |
;;    | 2024-10-21T13:47-04:00[America/New_York] | 494.6000 | 494.6700 | 494.5200 | 494.570 |   27389 |
;;    | 2024-10-21T13:48-04:00[America/New_York] | 494.5600 | 494.6800 | 494.5300 | 494.680 |   27603 |
;;    | 2024-10-21T13:49-04:00[America/New_York] | 494.6550 | 494.7200 | 494.6300 | 494.650 |   32059 |
;;    | 2024-10-21T13:50-04:00[America/New_York] | 494.6450 | 494.7500 | 494.6300 | 494.670 |   43491 |
;;    | 2024-10-21T13:51-04:00[America/New_York] | 494.6740 | 494.7700 | 494.6000 | 494.750 |   39712 |
;;    | 2024-10-21T13:52-04:00[America/New_York] | 494.7700 | 494.8000 | 494.6431 | 494.670 |   46377 |
;;    | 2024-10-21T13:53-04:00[America/New_York] | 494.6700 | 494.7700 | 494.6700 | 494.730 |   25242 |
;;    | 2024-10-21T13:54-04:00[America/New_York] | 494.7500 | 494.7800 | 494.7000 | 494.770 |   22312 |

(m/? (get-bars
      k
      {:asset "QQQ"
       :calendar [:us :d]}
      :full))
;; => kibot-bars [6446 6]:
;;    
;;    |                                    :date |   :open |   :high |     :low |  :close |  :volume |
;;    |------------------------------------------|--------:|--------:|---------:|--------:|---------:|
;;    | 1999-03-10T16:30-05:00[America/New_York] |  43.470 |  43.497 |  42.7530 |  43.338 |  6126478 |
;;    | 1999-03-11T16:30-05:00[America/New_York] |  43.736 |  43.989 |  42.7800 |  43.497 | 11364062 |
;;    | 1999-03-12T16:30-05:00[America/New_York] |  43.470 |  43.497 |  42.2220 |  42.673 | 10281822 |
;;    | 1999-03-15T16:30-05:00[America/New_York] |  42.912 |  43.842 |  42.4600 |  43.829 |  7454447 |
;;    | 1999-03-16T16:30-05:00[America/New_York] |  43.975 |  44.347 |  43.4970 |  44.055 |  5761413 |
;;    | 1999-03-17T16:30-05:00[America/New_York] |  44.161 |  44.214 |  43.7100 |  43.842 |  4661055 |
;;    | 1999-03-18T16:30-05:00[America/New_York] |  43.815 |  44.719 |  43.8150 |  44.693 |  5691685 |
;;    | 1999-03-19T16:30-05:00[America/New_York] |  45.277 |  45.277 |  43.5770 |  43.630 |  8333043 |
;;    | 1999-03-22T16:30-05:00[America/New_York] |  43.736 |  43.842 |  42.9390 |  43.085 |  5831263 |
;;    | 1999-03-23T16:30-05:00[America/New_York] |  42.780 |  42.939 |  41.4510 |  41.531 | 12714906 |
;;    |                                      ... |     ... |     ... |      ... |     ... |      ... |
;;    | 2024-10-04T16:30-04:00[America/New_York] | 487.450 | 487.880 | 482.3850 | 487.490 | 23543199 |
;;    | 2024-10-07T16:30-04:00[America/New_York] | 485.420 | 486.570 | 480.8700 | 482.060 | 18763913 |
;;    | 2024-10-08T16:30-04:00[America/New_York] | 484.660 | 489.990 | 483.8450 | 489.330 | 20587531 |
;;    | 2024-10-09T16:30-04:00[America/New_York] | 488.980 | 493.730 | 487.9500 | 493.170 | 17832092 |
;;    | 2024-10-10T16:30-04:00[America/New_York] | 490.850 | 494.470 | 489.5300 | 492.600 | 17056952 |
;;    | 2024-10-11T16:30-04:00[America/New_York] | 490.740 | 494.390 | 490.1700 | 493.340 | 15601802 |
;;    | 2024-10-14T16:30-04:00[America/New_York] | 495.760 | 498.830 | 495.2600 | 497.500 | 17133189 |
;;    | 2024-10-15T16:30-04:00[America/New_York] | 497.830 | 498.500 | 488.6800 | 490.780 | 26464531 |
;;    | 2024-10-16T16:30-04:00[America/New_York] | 491.180 | 491.690 | 487.5700 | 490.930 | 16817798 |
;;    | 2024-10-17T16:30-04:00[America/New_York] | 496.420 | 496.490 | 491.1901 | 491.350 | 21605573 |
;;    | 2024-10-18T16:30-04:00[America/New_York] | 494.060 | 495.570 | 493.3000 | 494.510 | 16035024 |

