(ns projekat.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [projekat.cleaning :as clean]))

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

(defn -main
  [& arg]
  (let [{:keys [header rows]} (process-data "resources/IMDbMovies.csv")]
    (println (str "Number of movies in this dataset: " (number-of-rows rows)))
    (println "All attributes in the dataset:")
    (println (str/join ", " header))
    (print-num-of-missing-values header rows)
    (println "===============================")
    (ratio-NA header rows)
    (println "===============================")
    (println "We need to clean values so that we can handle NA values.")
    ;;(println (get-all-currency-prefixes rows))
    (clean/process-and-save-data "resources/cleanedCSV.csv" header rows)
    ))

  



