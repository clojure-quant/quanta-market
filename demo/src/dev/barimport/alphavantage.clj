(ns dev.barimport.alphavantage
  (:require
   [clojure.pprint :refer [print-table]]
   [ta.helper.date :refer [parse-date]]
   [ta.import.provider.alphavantage.ds :as avds]
   [ta.import.provider.alphavantage.raw :as av]
   [ta.db.bars.protocol :refer [get-bars]]
   [dev.env :refer [secrets]]))

(def api-key (:alphavantage secrets))

(def avp (avds/create-import-alphavantage api-key))

(def dt (parse-date "2024-02-01"))

(get-bars avp {:asset "EURUSD"
               :calendar [:fx :d]}
          {:start (parse-date "2023-09-01")
           :mode :append})
;; => _unnamed [100 6]:
;;    
;;    |  :open |  :high |   :low | :close | :volume |            :date |
;;    |-------:|-------:|-------:|-------:|--------:|------------------|
;;    | 1.0848 | 1.0867 | 1.0842 | 1.0848 |     0.0 | 2024-05-26T00:00 |
;;    | 1.0862 | 1.0889 | 1.0859 | 1.0862 |     0.0 | 2024-05-27T00:00 |
;;    | 1.0852 | 1.0859 | 1.0810 | 1.0852 |     0.0 | 2024-05-28T00:00 |
;;    | 1.0802 | 1.0846 | 1.0789 | 1.0802 |     0.0 | 2024-05-29T00:00 |
;;    | 1.0835 | 1.0883 | 1.0812 | 1.0835 |     0.0 | 2024-05-30T00:00 |
;;    | 1.0853 | 1.0891 | 1.0828 | 1.0853 |     0.0 | 2024-06-02T00:00 |
;;    | 1.0907 | 1.0918 | 1.0861 | 1.0907 |     0.0 | 2024-06-03T00:00 |
;;    | 1.0882 | 1.0890 | 1.0861 | 1.0882 |     0.0 | 2024-06-04T00:00 |
;;    | 1.0875 | 1.0899 | 1.0864 | 1.0875 |     0.0 | 2024-06-05T00:00 |
;;    | 1.0894 | 1.0902 | 1.0805 | 1.0894 |     0.0 | 2024-06-06T00:00 |
;;    |    ... |    ... |    ... |    ... |     ... |              ... |
;;    | 1.1177 | 1.1200 | 1.1127 | 1.1177 |     0.0 | 2024-09-26T00:00 |
;;    | 1.1170 | 1.1208 | 1.1136 | 1.1170 |     0.0 | 2024-09-29T00:00 |
;;    | 1.1137 | 1.1146 | 1.1061 | 1.1137 |     0.0 | 2024-09-30T00:00 |
;;    | 1.1064 | 1.1082 | 1.1034 | 1.1064 |     0.0 | 2024-10-01T00:00 |
;;    | 1.1049 | 1.1049 | 1.1010 | 1.1049 |     0.0 | 2024-10-02T00:00 |
;;    | 1.1036 | 1.1040 | 1.0956 | 1.1036 |     0.0 | 2024-10-03T00:00 |
;;    | 1.0976 | 1.0997 | 1.0962 | 1.0976 |     0.0 | 2024-10-07T00:00 |
;;    | 1.0976 | 1.0981 | 1.0941 | 1.0976 |     0.0 | 2024-10-08T00:00 |
;;    | 1.0943 | 1.0954 | 1.0914 | 1.0943 |     0.0 | 2024-10-09T00:00 |
;;    | 1.0939 | 1.0940 | 1.0935 | 1.0937 |     0.0 | 2024-10-10T00:00 |
;;    | 1.0934 | 1.0936 | 1.0932 | 1.0934 |     0.0 | 2024-10-11T00:00 |

(get-bars avp {:asset "FMCDX"
               :calendar [:us :d]}
          {:start dt
           :mode :append})
;; => _unnamed [100 6]:
;;    
;;    | :open | :high |  :low | :close | :volume |            :date |
;;    |------:|------:|------:|-------:|--------:|------------------|
;;    | 41.42 | 41.42 | 41.42 |  41.42 |     0.0 | 2024-05-17T00:00 |
;;    | 41.47 | 41.47 | 41.47 |  41.47 |     0.0 | 2024-05-20T00:00 |
;;    | 41.44 | 41.44 | 41.44 |  41.44 |     0.0 | 2024-05-21T00:00 |
;;    | 41.13 | 41.13 | 41.13 |  41.13 |     0.0 | 2024-05-22T00:00 |
;;    | 40.66 | 40.66 | 40.66 |  40.66 |     0.0 | 2024-05-23T00:00 |
;;    | 41.10 | 41.10 | 41.10 |  41.10 |     0.0 | 2024-05-24T00:00 |
;;    | 40.87 | 40.87 | 40.87 |  40.87 |     0.0 | 2024-05-28T00:00 |
;;    | 40.37 | 40.37 | 40.37 |  40.37 |     0.0 | 2024-05-29T00:00 |
;;    | 40.73 | 40.73 | 40.73 |  40.73 |     0.0 | 2024-05-30T00:00 |
;;    | 41.11 | 41.11 | 41.11 |  41.11 |     0.0 | 2024-05-31T00:00 |
;;    |   ... |   ... |   ... |    ... |     ... |              ... |
;;    | 42.79 | 42.79 | 42.79 |  42.79 |     0.0 | 2024-09-25T00:00 |
;;    | 43.07 | 43.07 | 43.07 |  43.07 |     0.0 | 2024-09-26T00:00 |
;;    | 43.11 | 43.11 | 43.11 |  43.11 |     0.0 | 2024-09-27T00:00 |
;;    | 43.17 | 43.17 | 43.17 |  43.17 |     0.0 | 2024-09-30T00:00 |
;;    | 42.74 | 42.74 | 42.74 |  42.74 |     0.0 | 2024-10-01T00:00 |
;;    | 42.80 | 42.80 | 42.80 |  42.80 |     0.0 | 2024-10-02T00:00 |
;;    | 42.64 | 42.64 | 42.64 |  42.64 |     0.0 | 2024-10-03T00:00 |
;;    | 43.10 | 43.10 | 43.10 |  43.10 |     0.0 | 2024-10-04T00:00 |
;;    | 42.77 | 42.77 | 42.77 |  42.77 |     0.0 | 2024-10-07T00:00 |
;;    | 42.76 | 42.76 | 42.76 |  42.76 |     0.0 | 2024-10-08T00:00 |
;;    | 43.03 | 43.03 | 43.03 |  43.03 |     0.0 | 2024-10-09T00:00 |

; select search
(av/search "S&P 500")
(print-table [:symbol :type :name] (av/search "BA"))
(print-table (av/search "Fidelity MSCI"))

(av/search "gld")

;; # stock series
(av/get-daily :compact "QQQ")
(print-table (->> (av/get-daily :full "MSFT")
                  :series
                  ;reverse
                  (take 5)))

(->
 (av/get-daily-adjusted :compact "QQQ")
 :series
 ;first
 last)

(print-table (->> (av/get-daily-adjusted :full "MSFT")
                  :series
                  ;reverse
                  (take 5)))

;; # fx series
(print-table (take 5 (reverse (av/get-daily-fx :compact "EURUSD"))))

;; # crypto series
(print-table (take 5 (reverse (av/get-daily-crypto :compact "BTC"))))

; crypto rating
(av/get-crypto-rating "BTC")

(print-table
 (map av/get-crypto-rating ["BTC" "ETH" "LTC" "DASH"
                            "NANO" "EOS" "XLM"]))


