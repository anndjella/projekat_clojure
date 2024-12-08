(ns projekat.cleaning
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]))

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

(defn process-and-save-data 
  "Processing and saving data in csv file"
  [output-file header rows]
  (let [cleaned-rows (map #(clean-budget %) rows)
        header-with-budget (conj header :Budget-Cleaned)]
    (with-open [writer (io/writer output-file)]
      (csv/write-csv writer
                     (cons header-with-budget
                           (map #(map-row % header-with-budget) cleaned-rows))))))
