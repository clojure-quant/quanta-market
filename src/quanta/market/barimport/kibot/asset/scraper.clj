(ns quanta.market.barimport.kibot.asset.scraper
  (:require
   [taoensso.telemere :as tm]
   [missionary.core :as m]
   [hickory.core :as hc]
   [hickory.select :as s]
   [clj-commons.byte-streams :as bs]
   [aleph.http.client-middleware :as middleware]
   [babashka.fs :as fs]
   [quanta.market.util.aleph :as a]))

;; get/post with cookies

(defn get-with-cookies [cs url opts]
  (let [opts (merge {:cookie-store cs}
                    opts)]
    (m/sp
     (let [response (m/? (a/http-get url opts))
           {:keys [headers body]} response
           body-str (bs/to-string body)]
       body-str))))

(defn post-with-cookies [cs url opts]
  (m/sp
   (let [response (m/? (a/http-post url (merge {:cookie-store cs}
                                               opts)))
         {:keys [headers body]} response
         body-str (bs/to-string body)]
     body-str)))

;; extract form params

(defn select-input [htree]
  (s/select  (s/child
              (s/tag :input)) htree))

(defn id-value [row]
  [(:id row) (:value row)])

(defn select-id [id htree]
  (-> (s/select  (s/child
                  (s/attr :id #(= id %))) htree)
      first
      :attrs
      id-value))

(defn extract-form-params [htree]
  (into {}
        [(select-id "__EVENTVALIDATION" htree)
         (select-id "__VIEWSTATEGENERATOR" htree)
         (select-id "__VIEWSTATE" htree)]))

(defn html->form-params [html]
  (->> html
       hc/parse
       hc/as-hickory
       extract-form-params))

(defn get-login-params []
  (m/sp
   (let [cs (middleware/in-memory-cookie-store [])
         url "https://www.kibot.com/account.aspx"
         opts {}
         body (m/? (get-with-cookies cs url opts))]
     {:form-params (html->form-params body)
      :cs cs})))

(defn login-impl [{:keys [user password]} {:keys [cs form-params]}]
  (m/sp
   (let [opts {:form-params (merge form-params
                                   {"ctl00$Content$LoginView1$Login1$Password" password
                                    "ctl00$Content$LoginView1$Login1$RememberMe:" "on"
                                    "ctl00$Content$LoginView1$Login1$LoginButton" "Log+In"
                                    "ctl00$Content$LoginView1$Login1$UserName" user})}
         url "https://www.kibot.com/account.aspx"
         body (m/? (post-with-cookies cs url opts))]
     body)))

(defn login [{:keys [user password] :as api-creds}]
  (m/sp
   (let [lp (m/? (get-login-params))]
     (m/? (login-impl api-creds lp))
     lp)))

;; get asset list

(def calendars
  {[:forex :m] "https://www.kibot.com/downloadtext.aspx?product=5,All_Forex_Pairs_1min"
   [:forex :d] "https://www.kibot.com/downloadtext.aspx?product=4,All_Forex_Pairs_daily"
   [:futures :m]   "https://www.kibot.com/downloadtext.aspx?product=7,All_Futures_Contracts_1min"
   [:futures :d]   "https://www.kibot.com/downloadtext.aspx?product=6,All_Futures_Contracts_daily"
   [:etf :m]   "https://www.kibot.com/downloadtext.aspx?product=3,All_ETFs_1min"
   [:etf :d] "https://www.kibot.com/downloadtext.aspx?product=2,All_ETFs_daily"

   ;[:forex-htm :m] 
   [:stocks-htm :m] "https://www.kibot.com/download.aspx?product=1,All_Stocks_1min"})

(defn get-url [calendar]
  (get calendars calendar))

(def base-dir ".data/kibot-links/")

(defn calendar-filename [[category f]]
  (str base-dir (name category) "-" (name f) ".txt"))

(defn download-calendar-impl [lp cal-vec]
  (let [url (get-url cal-vec)
        [category f] cal-vec]
    (m/sp
     (let [url "https://www.kibot.com/download.aspx"
           url "https://www.kibot.com/download.aspx?product=0,All_Stocks_unadjusted_daily"
           ;opts {:query-params {:product "1,All_Stocks_1min"}}
           opts {}
           body (m/? (get-with-cookies (:cs lp) url opts))]
       (spit (calendar-filename cal-vec) body)))))

(defn download-calendars-impl2 [lp]
  (m/sp
   (let [cal-vecs (keys calendars)]
     (for [cal-vec cal-vecs]
       (m/? (download-calendar-impl lp cal-vec))))))

(defn download-calendars
  "downloads http-download-link files for all calendars
   and saves them to .data/kibot-web"
  [{:keys [user password] :as api-creds}]
  (m/sp
   (fs/create-dirs base-dir)
   (let [lp (m/? (login api-creds))
         cal-vecs (keys calendars)]
     (for [cal-vec cal-vecs]
       (m/? (download-calendar-impl lp cal-vec))))))

(defn download-calendar
  "downloads http-download-link files for all calendars
   and saves them to .data/kibot-web"
  [{:keys [user password] :as api-creds} cal-vec]
  (m/sp
   (fs/create-dirs base-dir)
   (let [lp (m/? (login api-creds))]
     (m/? (download-calendar-impl lp cal-vec)))))

(comment
  (def cs (middleware/in-memory-cookie-store []))

  (middleware/get-cookies cs)

  (def lp
    (get-login-params))

  (:form-params lp)
  (middleware/get-cookies (:cs lp))

  (login s lp)

  (get-url [:forex :m])

  (m/? (download-calendar lp [:etf :m]))

  (m/? (download-calendars lp))

;  
  )