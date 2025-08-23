(ns projekat.cleaning
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; ---------------------------
;; IO utilities & header handling
;; ---------------------------
(defn load-csv
  "Loads csv"
  [filename]
  (with-open [reader (io/reader filename)]
    (doall (csv/read-csv reader))))

(defn convert-to-keywords
  "Converts a sequence of strings to keywords"
  [header]
  (mapv #(keyword %) header))

(defn replace-spaces-with-dashes
  "Replaces spaces with dashes in a sequence of keywords"
  [keywords]
  (mapv #(keyword (str/replace (name %) #" " "-")) keywords))

(defn row-to-map
  "Converts a row to a map"
  [header row]
  (zipmap header row))

(defn process-data
  "Processes the csv file and returns a map with header and rows"
  [filename]
  (let [data (load-csv filename)
        header (-> (first data)
                   convert-to-keywords
                   replace-spaces-with-dashes)
        rows (rest data)
        mapped-data (map #(row-to-map header %) rows)]
    {:header header
     :rows mapped-data}))


(take 4 (:rows (process-data "resources/IMDbMovies.csv")) )
;; ----------------------------------------------------------
;; Missing values, duplicates, and helpers
;; ----------------------------------------------------------

(defn number-of-rows
  [rows]
 ;; (let [rows (:rows data)]
  (count rows))

(defn missing-check
  "Checks if a row has any missing values (nil or empty)" 
  [row]
  (some #(or (nil? %) (empty? %)) (vals row)))

(defn number-of-unique-elements
[column-key data]
(let [rows (:rows data)]
  (->> rows
       (map #(column-key %))
       (distinct)
       count)))
(number-of-unique-elements :Title (process-data "resources/IMDbMovies.csv"))

(defn repeated-elements
  "Returns elements in column that repeat (appear more than once)"
  [column-key data]
  (let [rows (:rows data)]  
    (->> rows
         (map #(column-key %)) 
         (frequencies)         
         (filter #(> (val %) 1))
         (map key)))) 

(defn count-repeated-elements
  "Returns number of elements in column that repeat"
  [column-key data]
  (let [rows (:rows data)]  
    (->> rows
         (map #(column-key %))  
         (frequencies)          
         (filter #(> (val %) 1)) 
         count)))                

(repeated-elements :Title (process-data "resources/IMDbMovies.csv"))
(count-repeated-elements :Title (process-data "resources/IMDbMovies.csv"))

(defn missing-in-column
  "Checks if there are missing values in the specified column across all rows"
  [column-key rows]
  (some #(let [value (get % column-key)]
           (or (nil? value) (empty? value)))
        rows))

(defn missing-values-in-column
  "Returns the count of missing values in a specific column"
  [column-key rows]
  (count (filter #(or (nil? (column-key %)) (empty? (column-key %))) rows)))

(defn print-num-of-missing-values
  "Prints number of missing values in each column"
  [header rows]
  (doseq [x header]
    (println (str "Missing values for " x ": " (missing-values-in-column x rows)))))


(defn ratio-NA
  "Returns a map of ratios of missing values to total values for each column in the header"
  [header rows]
 (doseq [column header]
 (let [missing-in-column (missing-values-in-column column rows)
       total-rows (number-of-rows rows)
       percent (if (zero? total-rows)
       0
       (* (/ (float missing-in-column) total-rows) 100) )]
       (println (str "Column: " column ", Percent of missing values: " percent)))))

;;mozda treba izbaciti sledece varijable zbog postojanja velikog broja nedostajucih vrednosti:
;;Budget, 35.27% svih vrednosti je NA
;;Gross-in-US-&-Canada, 33.24% svih vrednosti je NA
;;Opening-Weekend-Gross-in-US-&-Canada, 37.3% svih vrednosti je NA

;;all columns are type string, so they must be cleaned and converted
;;to their appropriate types for accurate analysis and processing. Then NA values
;;must be handled

(defn get-column
  "Function needed in another functions for getting column by its name"
  [column-key rows]
  (mapv #(get % column-key)rows))

(defn extract-currency-prefix
  "Extracts the prefix or initial part of the budget value"
  [budget]
  (let [prefix (str/trim (subs budget 0 (min 3 (count budget))))]
    (if (empty? prefix)
      nil
      prefix)))

(defn get-all-currency-prefixes
  "Returns a list of all unique currency prefixes from the Budget column"
  [rows]
  (let [prefixes (map (fn [row] (extract-currency-prefix (get row :Budget))) rows)]
    (distinct (remove nil? prefixes))))

;;there are many different currencies and we need to transform them to only one
;;so that prediction can be cosistent
;;DKKÂ $ ₹ ₩ € IDR £ FRF sek dok nok rur dem ca$ fim a$ CN¥ vmr
;; bef nz$ nt$ r$ thb nlg czk dkk frf ats

;;because Opening-Weekend-Gross-in-US-&-Canada has 37.3% of all values NA, 
;;it won't be included in analysis


(defn get-all-distinct-rated
  [rows]
  (let [rated (map :Motion-Picture-Rating rows)]
   (distinct (remove nil? rated))))
;;Motion-Picture-Rating column has a lot of distinct values (29) for categorical feature 
;;also it has a lot of missing values (8.8%) so it will be dismissed from analysis

;;also Summary, Director, Title and Writer features will be dismissed because they are string
;;and hard to convert to something categorical or numerical

;; ----------------------------------------------------------
;; Parsing & feature engineering
;; ----------------------------------------------------------

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
  (if (or (nil? budget) (str/blank? budget) (not (re-find #"\d" budget)))
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
  [row column-key]
  (let [budget (get row (keyword column-key))
        cleaned-budget (parse-budget budget)]
    (assoc row (keyword (str column-key "-Cleaned"))  cleaned-budget)))


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

(defn parse-genres
  [row]
  (let [genres (get row :Main-Genres)]
    (if (or (empty? genres) (str/blank? genres) )
       []
      (remove str/blank? (distinct (str/split genres #","))))
    ))
 ;;(map #(str/split % #",") row))

(defn extract-distinct-genres
  [rows]
  (distinct (flatten (map #(parse-genres %) rows))))

(defn create-genre-map
  "Creates a map with all genres set to 0"
  [genres]
  (zipmap (map keyword genres) (repeat 0)))

(defn encode-genres
  "Creates one-hot encoded map for given genres"
  [all-genres row-genres]
  (reduce #(assoc %1 (keyword %2) 1)
          (create-genre-map all-genres)
          row-genres))

(defn add-genre-columns
  "Adds one-hot encoded genre columns to the row"
  [row all-genres]
  (let [row-genres (parse-genres row)
        genre-encoding (encode-genres all-genres row-genres)]
    (merge row genre-encoding)))

(defn process-and-save-data
  "Processing and saving data in csv file"
  [output-file header rows]
  (let [all-genres (extract-distinct-genres rows)
        cleaned-rows ;;(map #(clean-budget %) rows)
        (map #(-> %
                  (clean-budget "Budget")
                  (clean-budget "Gross-in-US-&-Canada")
                  (clean-budget "Gross-worldwide")
                  (clean-rating )
                  (clean-runtime)
                  (clean-num-of-ratings) 
                  (add-genre-columns all-genres)
                  (dissoc :Budget :Rating :Runtime :Number-of-Ratings 
                          :Gross-in-US-&-Canada :Gross-worldwide :Main-Genres
                          :Opening-Weekend-Gross-in-US-&-Canada
                          :Summary :Writer :Director :Title :Motion-Picture-Rating ))
             rows) 
        genre-keywords (map keyword all-genres)
        header-with-budget (->> header
                              (remove #{:Budget :Rating :Runtime :Number-of-Ratings 
                                        :Gross-in-US-&-Canada :Gross-worldwide :Main-Genres 
                                        :Opening-Weekend-Gross-in-US-&-Canada 
                                        :Summary :Writer :Director :Title :Motion-Picture-Rating })
                              (concat [:Budget-Cleaned :Rating-Cleaned :Runtime-Cleaned
                                       :Num-of-Ratings-Cleaned :Gross-in-US-&-Canada-Cleaned 
                                       :Gross-worldwide-Cleaned] genre-keywords)
                               vec)]
    (with-open [writer (io/writer output-file)]
      (csv/write-csv writer
                     (cons header-with-budget
                           (map #(map-row % header-with-budget) cleaned-rows))))))
