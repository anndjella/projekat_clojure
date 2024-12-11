(ns projekat.cleaning
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [projekat.cleaning :as clean]))

(defn detect-currency [value]
    (cond
      (re-matches #".*\$.*" value) :usd
      (re-matches #".*₹.*" value) :inr
      (re-matches #".*¥.*" value) :jpy
      (re-matches #".*₩.*" value) :krw
      (re-matches #".*€.*" value) :eur
      (re-matches #".*IDR.*" value) :idr
      (re-matches #".*ITL.*" value) :itl
      (re-matches #".*£.*" value) :gbp
      (re-matches #".*FRF.*" value) :frf
      (re-matches #".*SEK.*" value) :sek
      (re-matches #".*NOK.*" value) :nok
      (re-matches #".*RUR.*" value) :rur
      (re-matches #".*DEM.*" value) :dem
      (re-matches #".*CA\$.*" value) :cad
      (re-matches #".*FIM.*" value) :fim
      (re-matches #".*A\$.*" value) :aud
      (re-matches #".*CN¥.*" value) :cny
      (re-matches #".*VMR.*" value) :vmr
      (re-matches #".*BEF.*" value) :bef
      (re-matches #".*MVR.*" value) :mvr
      (re-matches #".*ESP.*" value) :esp
      (re-matches #".*NZ\$.*" value) :nzd
      (re-matches #".*NT\$.*" value) :ntd
      (re-matches #".*R\$.*" value) :brl
      (re-matches #".*THB.*" value) :thb
      (re-matches #".*NLG.*" value) :nlg
      (re-matches #".*CZK.*" value) :czk
      (re-matches #".*DKK.*" value) :dkk
      (re-matches #".*FRF.*" value) :frf
      (re-matches #".*ATS.*" value) :ats
      (re-matches #".*DOP.*" value) :dop
      :else nil))
 
(def conversion-rates
  {:usd 1
   :inr 0.012   ;;  1 INR = 0.012 USD
   :krw 0.00071
   :eur 1.06
   :jpy 0.0066
   :idr 0.000063
   :itl 0.00054
   :gbp 1.2
   :frf 0.16
   :sek 0.091
   :nok 0.09
   :rur 0.0095
   :dem 0.0034
   :cad 0.71
   :fim 0.18
   :aud 0.64
   :cny 0.14
   :vmr 0.035
   :bef 0.026
   :mvr 0.065
   :esp 0.0063
   :nzd 0.59
   :ntd 0.031
   :brl 0.17
   :thb 0.029
   :nlg 0.48
   :czk 0.042
   :dkk 0.14
   :ats 0.0076
   :dop 0.017
   })

(defn parse-budget [budget]
  (if (or (nil? budget) (str/blank? budget))
    nil
    (let [currency (detect-currency budget)
          raw-value (->> budget
                         (re-seq #"[0-9.]+")
                         (apply str)
                         (Double/parseDouble))]
      (if currency
        (* raw-value (get conversion-rates currency 1))
        nil))))

(defn clean-budget
  "Creates new row in a map with cleaned values from Budget column"
  [row]
  (let [budget (get row :Budget)
        cleaned-budget (parse-budget budget)]
    (assoc row :Budget-Cleaned cleaned-budget)))

(defn map-row 
  "Returns values in order by keys"
  [row header-with-budget]
  (map #(get row %) header-with-budget))



(defn parse-rating
  [rating]
  (if (or (nil? rating) (str/blank? rating))
    nil
    (let [rat (-> rating
                  (str/split #"/")
                  (first)
                  (Double/parseDouble))]
      (if rat
        rat
        nil))))

;; (first (str/split "4.5/10" #"/"))

(defn clean-rating
  "Creates new row in a map with cleaned values from Rating column"
  [row]
  (let [rating (get row :Rating)
        cleaned-rating (parse-rating rating)]
    (assoc row :Rating-Cleaned cleaned-rating)))

;; (re-matches #"(\d+)h (\d+)m" "1h 3m")

(defn parse-runtime
  "Converts runtime string (like 2h 12m) to minutes"
  [runtime-str]
  (if (or (nil? runtime-str) (str/blank? runtime-str))
    nil
    (let [hours (when-let [hrs (re-find #"(\d+)h" runtime-str)]
                  (Integer/parseInt (second hrs)))
          minutes (when-let [mins (re-find #"(\d+)m" runtime-str)]
                    (Integer/parseInt (second mins)))]
      (+ (* (or hours 0) 60) (or minutes 0)))))

(defn clean-runtime
  "Creates new row in a map with cleaned values from Runtime column"
  [row]
  (let [runtime (get row :Runtime)
        cleaned-r (parse-runtime runtime)]
    (assoc row :Runtime-Cleaned cleaned-r)))

(defn parse-num-of-ratings
  "Leaves only number in number of ratings"
  [str] 
  (if(or (nil? str) (str/blank? str)) 
      nil
   (if (str/includes? str "K")
     (int (* (Double/parseDouble (first (str/split,str, #"K"))) 1000))
     (if (str/includes? str "M")
      (int (* (Double/parseDouble (first (str/split,str, #"M"))) 1000000))
       (int (Double/parseDouble str))))))

(defn clean-num-of-ratings
  "Creates new row in a map with cleaned values from Number-of-Ratings column"
  [row]
  (let [runtime (get row :Number-of-Ratings)
        cleaned-num (parse-num-of-ratings runtime)]
    (assoc row :Num-of-Ratings-Cleaned cleaned-num)))

;; (first (str/split,"34K", #"K"))

(defn process-and-save-data
  "Processing and saving data in csv file"
  [output-file header rows]
  (let [cleaned-rows ;;(map #(clean-budget %) rows)
        (map #(-> %
                  (clean-budget)
                  (clean-rating)
                  (clean-runtime)
                  (clean-num-of-ratings)
                  (dissoc :Budget :Rating :Runtime :Number-of-Ratings))
             rows)
        header-with-budget (->> header
                              (remove #{:Budget :Rating :Runtime :Number-of-Ratings})
                              (concat [:Budget-Cleaned :Rating-Cleaned :Runtime-Cleaned :Num-of-Ratings-Cleaned])
                               vec)]
    (with-open [writer (io/writer output-file)]
      (csv/write-csv writer
                     (cons header-with-budget
                           (map #(map-row % header-with-budget) cleaned-rows))))))
