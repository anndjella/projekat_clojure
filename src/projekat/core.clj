(ns projekat.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]))

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
  [data]
  (let [rows (:rows data)]
  (count rows)))


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

(defn -main
  [& args]
  (let [{:keys [header rows]} (process-data "resources/IMDbMovies.csv")]
    (println (str "Number of movies in this dataset: " (number-of-rows (process-data "resources/IMDbMovies.csv"))))
    (println "All attributes in the dataset:")
    (println (str/join ", " header))
    (println "Missing values in 'Title':" (missing-values-in-column :Title rows))
    (println "Missing values in 'Summary':" (missing-values-in-column :Summary rows))
    (println "Missing values in 'Director':" (missing-values-in-column :Director rows))
    (println "Missing values in 'Writer':" (missing-values-in-column :Writer rows))
    (println "Missing values in 'Main-Genres':" (missing-values-in-column :Main-Genres rows))
    (println "Missing values in 'Motion-Picture-Rating':" (missing-values-in-column :Motion-Picture-Rating rows))
    (println "Missing values in 'Runtime':" (missing-values-in-column :Runtime rows))
    (println "Missing values in 'Release-Year':" (missing-values-in-column :Release-Year rows))
    (println "Missing values in 'Rating':" (missing-values-in-column :Rating rows))
    (println "Missing values in 'Number-of-Ratings':" (missing-values-in-column :Number-of-Ratings rows))
    (println "Missing values in 'Budget':" (missing-values-in-column :Budget rows))
    (println "Missing values in 'Gross-in-US-&-Canada':" (missing-values-in-column :Gross-in-US-&-Canada rows))
    (println "Missing values in 'Gross-worldwide':" (missing-values-in-column :Gross-worldwide rows))
    (println "Missing values in 'Opening-Weekend-Gross-in-US-&-Canada':" (missing-values-in-column :Opening-Weekend-Gross-in-US-&-Canada rows))))
  



