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



(defn -main
  [& args]
  (let [{:keys [header rows]} (process-data "resources/IMDbMovies.csv")]
    (println "All attributes in the dataset:")
    (println (str/join ", " header))
    (println "First 5 rows in dataset: " (take 5 rows))
    ))
  



