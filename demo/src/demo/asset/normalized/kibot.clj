(ns demo.asset.normalized.kibot
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [missionary.core :as m]
   [quanta.market.barimport.kibot.asset.db :as db]))

(defn load-raw [category]
  (-> (str "../resources/asset/raw/kibot-" (name category) ".edn")
      slurp
      edn/read-string))

(-> (load-raw :forex)
    first)

;; => {:kibot "EURNOK",
;;     :kibot-link-d
;;     "i3idizinikiecbipiniviziki5abbrizabijihcbi6iki5ihafbri7iei3i8ili7ivizihi8cbbgbrivabisirikigcbcdcvcjcgcscnbrinieizihi5i4i3igcbi8i3inigabbrivizi3i5izi8i3izihcbbsb2bsb2bsb7b7bgbri8ini5ihidizcbbsbri3izizi3idipisihieizcbbsbri5ihimi7igi3i5ivihivivinikiecbbgbri7ivihi5cbipikihi5izigihipieihi5caimisi3inigblidikisbriji3ivivi9iki5i8cba6arafavanaj7n72",
;;     :kibot-link-m
;;     "i3idizinikiecbipiniviziki5abbrizabijihcbi6iki5ihafbri7iei3i8ili7ivizihi8cbbgbrivabisirikigcbcdcvcjcgcscnbrinieizihi5i4i3igcbbsbrivizi3i5izi8i3izihcbbsb2bsb2bsb7b7bgbri8ini5ihidizcbbsbri3izizi3idipisihieizcbbsbri5ihimi7igi3i5ivihivivinikiecbbgbri7ivihi5cbipikihi5izigihipieihi5caimisi3inigblidikisbriji3ivivi9iki5i8cba6arafavanaj7n72"}

(defn normalize-forex [{:keys [kibot] :as row}]
  (let [asset (str (subs kibot 0 3) "/" (subs kibot 3))]
    (assoc row
           :asset asset
           :category :forex)))

(-> (load-raw :forex)
    first
    normalize-forex)
;; => {:kibot "EURNOK",
;;     :kibot-link-d
;;     "i3idizinikiecbipiniviziki5abbrizabijihcbi6iki5ihafbri7iei3i8ili7ivizihi8cbbgbrivabisirikigcbcdcvcjcgcscnbrinieizihi5i4i3igcbi8i3inigabbrivizi3i5izi8i3izihcbbsb2bsb2bsb7b7bgbri8ini5ihidizcbbsbri3izizi3idipisihieizcbbsbri5ihimi7igi3i5ivihivivinikiecbbgbri7ivihi5cbipikihi5izigihipieihi5caimisi3inigblidikisbriji3ivivi9iki5i8cba6arafavanaj7n72",
;;     :kibot-link-m
;;     "i3idizinikiecbipiniviziki5abbrizabijihcbi6iki5ihafbri7iei3i8ili7ivizihi8cbbgbrivabisirikigcbcdcvcjcgcscnbrinieizihi5i4i3igcbbsbrivizi3i5izi8i3izihcbbsb2bsb2bsb7b7bgbri8ini5ihidizcbbsbri3izizi3idipisihieizcbbsbri5ihimi7igi3i5ivihivivinikiecbbgbri7ivihi5cbipikihi5izigihipieihi5caimisi3inigblidikisbriji3ivivi9iki5i8cba6arafavanaj7n72",
;;     :asset "EUR/NOK",
;;     :category :forex}

(defn save-category [c assets]
  (spit (str "../resources/asset/kibot-" (name c) ".edn") (pr-str assets)))

;; forex - we keep all pairs, as the asset has a / so it is unique

(->> (load-raw :forex)
     (map normalize-forex)
     (save-category :forex))

;; etf

(->> (load-raw :etf)
     ;first
     ;(map normalize-forex)
     ;(save-category :etf)
     (map :kibot)
     (group-by count)
     keys)
;; => (4 3 2 5)

;; etfs have up to 5 digits, so there is no conflict with bybit which has 6

(defn normalize-etf [{:keys [kibot] :as row}]
  (let [asset kibot]
    (assoc row
           :asset asset
           :category :etf)))

(->> (load-raw :etf)
     (map normalize-etf)
     (save-category :etf))

;; future

"ACH20"

; https://www.cmegroup.com/month-codes.html
; CMES_CODE_TO_MONTH = dict(zip("FGHJKMNQUVXZ", range(1, 13)))
;MONTH_TO_CMES_CODE = dict(zip(range(1, 13), "FGHJKMNQUVXZ"))

(defn is-future? [s]
  (when-let [m (re-matches #"(.*)([FGHJKMNQUVXZ])(\d\d.*)" s)]
    (let [[_ root month year] m]
      {:asset s
       :month month
       :year (parse-long year)
       :future-root (str root "0")})))

(is-future? "ACH20")

(->> (load-raw :futures)
    ;first
    ;normalize-forex

     (map :kibot)
     sort
     ;(map is-future?)
     (remove is-future?)

     ;(filter #(str/starts-with? "NG" %))
     count)
;; => 82

(defn future-asset? [{:keys [kibot]}]
  (is-future? kibot))

(defn normalize-future [{:keys [kibot] :as row}]
  (let [asset kibot]
    (assoc row
           :asset (str asset "0")
           :future asset
           :category :future)))

(->> (load-raw :futures)
     (remove future-asset?)
     (map normalize-future)
     (save-category :future))