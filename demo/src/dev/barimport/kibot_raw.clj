(ns dev.barimport.kibot-raw
  (:require
   [missionary.core :as m]
   [quanta.market.barimport.kibot.raw :as kb]
   [dev.env :refer [secrets]]))

(def k (:kibot secrets))

k
;; => {:user "hoertlehner@gmail.com", :password "282m2fhgh"}

(m/?
 (kb/login k))

(m/?
 (kb/status))

(m/?
 (kb/history k {:symbol "AAPL"
                :interval "daily"
                :period 2}))

;; => "10/08/2024,224.3,225.98,223.25,225.78,23003386\r\n10/09/2024,225.31,229.75,224.83,229.49,25029040\r\n"

(m/?
 (kb/history k {:symbol "AAPL"
                :interval 60
                :period 1}))
;; => Execution error (ExceptionInfo) at quanta.market.barimport.kibot.raw/make-request$cr68446-block-30 (REPL:60).
;;    
;;    Your account does not have full access to the API.
;;    
;;    You can use the 'guest' account for testing and to download daily data for stocks and ETFs.
;;    For more information, please visit http://www.kibot.com/api/
;;    
;;    Please visit http://www.kibot.com/updates.aspx to see how to subscribe.

(m/?
 (kb/history
  k
  {:symbol "SIL" ; SIL - ETF
   :interval "daily"
   :period 4
   :type "ETF" ; Can be stocks, ETFs forex, futures.
   :timezone "UTC"
   :splitadjusted 1}))

;; => "10/09/2024,34.8,35.07,34.4201,34.99,411814\r\n"

; futures
     ; http://www.kibot.com/historical_data/Futures_Historical_Tick_with_Bid_Ask_Data.aspx

(m/?
 (kb/history
  k
  {:symbol "SIL" ; SIL - FUTURE
   :type "futures" ; Can be stocks, ETFs forex, futures.
   :interval "daily"
   :period 5
   :timezone "UTC"
   :splitadjusted 1}))
;; => "10/09/2024,30.88,31.005,30.44,30.67,17294\r\n"

(m/?
 (kb/history
  k
  {:symbol "SIL" ; SIL - FUTURE
   :type "futures" ; Can be stocks, ETFs forex, futures.
   :interval "daily"
   :startdate "2023-09-01"
   :enddate "2023-09-20"
   :timezone "UTC"
   :splitadjusted 1}))
;; => "09/01/2023,24.81,25.22,24.515,24.562,9019\r\n09/05/2023,24.625,24.635,23.815,23.873,12488\r\n09/06/2023,23.89,23.93,23.32,23.503,8688\r\n09/07/2023,23.48,23.49,23.13,23.24,6254\r\n09/08/2023,23.255,23.45,23.135,23.174,7591\r\n09/11/2023,23.21,23.515,23.18,23.383,6052\r\n09/12/2023,23.375,23.475,23.11,23.402,6571\r\n09/13/2023,23.37,23.39,23.025,23.181,6561\r\n09/14/2023,23.125,23.26,22.56,22.994,12133\r\n09/15/2023,22.935,23.585,22.91,23.386,11305\r\n09/18/2023,23.32,23.535,23.23,23.498,8162\r\n09/19/2023,23.505,23.71,23.37,23.456,6979\r\n09/20/2023,23.48,23.895,23.325,23.836,10244\r\n"

(m/?
 (kb/history
  k
  {:type "forex"
   :symbol "EURUSD"
   :startdate "2023-09-01"
   :interval "daily"
   :timezone "UTC"}))
;; => "09/01/2023,1.08435,1.0882,1.0772,1.07731,252561\r\n09/04/2023,1.07765,1.08088,1.07716,1.07938,100151\r\n09/05/2023,1.07939,1.07983,1.07059,1.0722,214750\r\n09/06/2023,1.07221,1.07488,1.07023,1.07269,213766\r\n09/07/2023,1.07272,1.07317,1.06858,1.06957,186169\r\n09/08/2023,1.06966,1.07438,1.0694,1.06982,177467\r\n09/11/2023,1.07145,1.07593,1.07072,1.07502,196109\r\n09/12/2023,1.07501,1.07679,1.07055,1.07538,173051\r\n09/13/2023,1.07538,1.07649,1.07107,1.07292,242969\r\n09/14/2023,1.0729,1.07521,1.06318,1.06429,264818\r\n09/15/2023,1.06427,1.06878,1.06333,1.06596,199085\r\n09/18/2023,1.06582,1.06986,1.06545,1.06915,150709\r\n09/19/2023,1.06915,1.07179,1.0675,1.06792,164650\r\n09/20/2023,1.06792,1.07368,1.065,1.06605,227166\r\n09/21/2023,1.06601,1.06736,1.06167,1.06579,252028\r\n09/22/2023,1.06576,1.06717,1.06147,1.06452,206716\r\n09/25/2023,1.06504,1.06556,1.05751,1.0593,177771\r\n09/26/2023,1.05928,1.06091,1.0562,1.05722,190248\r\n09/27/2023,1.0572,1.05743,1.04881,1.05022,231364\r\n09/28/2023,1.05026,1.05788,1.04909,1.05654,272198\r\n09/29/2023,1.05652,1.06171,1.05565,1.05739,264334\r\n10/02/2023,1.05655,1.05918,1.04766,1.04771,240182\r\n10/03/2023,1.04773,1.04935,1.04482,1.04663,291047\r\n10/04/2023,1.04664,1.05322,1.04513,1.05036,273791\r\n10/05/2023,1.05034,1.05518,1.04999,1.05468,261147\r\n10/06/2023,1.05473,1.06,1.04821,1.05844,"

(m/?
 (kb/history
  k {:type "forex",
     :symbol "EURUSD",
                 ;:startdate "2023-09-01",
     :period 1
     :interval "1" ; "daily"
     :timezone "UTC"}))
;; => Execution error (ExceptionInfo) at quanta.market.barimport.kibot.raw/make-request$cr68446-block-30 (REPL:60).
;;    
;;    Your account does not have full access to the API.
;;    
;;    You can use the 'guest' account for testing and to download daily data for stocks and ETFs.
;;    For more information, please visit http://www.kibot.com/api/
;;    
;;    Please visit http://www.kibot.com/updates.aspx to see how to subscribe.

(m/?
 (kb/history
  k {:symbol "JY"
     :type "futures" ; Can be stocks, ETFs forex, futures.
     :interval "daily" ; 5 ; 5 minute bars
     :period 5 ; number of days going back
     :timezone "UTC"}))
    ;; the following error happens if we want 1 day back, but 1 day back is a holiday.
    ;; => {:error {:code "405", :title "Data Not Found.", :message "No data found for the specified period for JY."}}
    ;; => "11/20/2023,0.0067125,0.006782,0.0066965,0.006772,245135\r\n11/21/2023,0.0067695,0.0068235,0.006757,0.0067695,234151\r\n11/22/2023,0.006769,0.0067805,0.0067015,0.006709,219589\r\n"

(m/?
 (kb/history
  k
  {:symbol "JY"
   :type "futures" ; Can be stocks, ETFs forex, futures.
   :interval "1" ; 1 ; 5 ; 5 minute bars
   :period 10 ; number of days going back
   :timezone "UTC"}))
;; => Execution error (ExceptionInfo) at quanta.market.barimport.kibot.raw/make-request$cr68446-block-30 (REPL:60).
;;    
;;    Your account does not have full access to the API.
;;    
;;    You can use the 'guest' account for testing and to download daily data for stocks and ETFs.
;;    For more information, please visit http://www.kibot.com/api/
;;    
;;    Please visit http://www.kibot.com/updates.aspx to see how to subscribe.

