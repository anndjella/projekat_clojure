(ns projekat.dbWork
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql] 
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [projekat.imputation :as imputation]))

(def db-spec
  {:dbtype "sqlite"
   :dbname "resources/database.db"})

(defn create-movies-table []
  (jdbc/execute! db-spec 
                 ["CREATE TABLE movies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    budget_cleaned REAL,
    rating_cleaned REAL,
    runtime_cleaned INTEGER,
    num_of_ratings_cleaned INTEGER,
    gross_in_us_canada_cleaned REAL,
    gross_worldwide_cleaned REAL,
    action INTEGER CHECK(action IN (0,1)),
    adventure INTEGER CHECK(adventure IN (0,1)),
    biography INTEGER CHECK(biography IN (0,1)),
    drama INTEGER CHECK(drama IN (0,1)),
    crime INTEGER CHECK(crime IN (0,1)),
    animation INTEGER CHECK(animation IN (0,1)),
    comedy INTEGER CHECK(comedy IN (0,1)),
    family INTEGER CHECK(family IN (0,1)),
    horror INTEGER CHECK(horror IN (0,1)),
    mystery INTEGER CHECK(mystery IN (0,1)),
    thriller INTEGER CHECK(thriller IN (0,1)),
    history INTEGER CHECK(history IN (0,1)),
    fantasy INTEGER CHECK(fantasy IN (0,1)),
    sci_fi INTEGER CHECK(sci_fi IN (0,1)),
    romance INTEGER CHECK(romance IN (0,1)),
    music INTEGER CHECK(music IN (0,1)),
    sport INTEGER CHECK(sport IN (0,1)),
    musical INTEGER CHECK(musical IN (0,1)),
    documentary INTEGER CHECK(documentary IN (0,1)),
    war INTEGER CHECK(war IN (0,1)),
    western INTEGER CHECK(western IN (0,1)),
    film_noir INTEGER CHECK(film_noir IN (0,1)),
    release_year INTEGER
);
"]))

(defn parse-number [s]
  (if (str/includes? s ".")
    (Double/parseDouble s)
    (Long/parseLong s)))

(defn process-row [row]
  (vec (map parse-number row)))

(defn import-csv-to-db [db-spec csv-path] 
  (jdbc/execute! db-spec ["DELETE FROM movies"])
  (jdbc/execute! db-spec ["DELETE FROM sqlite_sequence WHERE name='movies'"])
  (with-open [reader (io/reader csv-path)]
    (let [data (csv/read-csv reader)
          header (first data)
          rows (rest data)]

      (doseq [row rows]
        (let [processed-row (process-row row)
              sql-params (concat ["INSERT INTO movies (
                                  budget_cleaned, 
                                  rating_cleaned, 
                                  runtime_cleaned, 
                                  num_of_ratings_cleaned, 
                                  gross_in_us_canada_cleaned, 
                                  gross_worldwide_cleaned,
                                  action, adventure, biography, drama, 
                                  crime, animation, comedy, family, 
                                  horror, mystery, thriller, history,
                                  fantasy, sci_fi, romance, music,
                                  sport, musical, documentary, war,
                                  western, film_noir, release_year
                                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                                         ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                                         ?, ?, ?, ?, ?, ?, ?, ?, ?)"]
                                 processed-row)]
          (try
            (jdbc/execute! db-spec sql-params)
            (catch Exception e
              (println "Error importing row:" row)
              (println "Error message:" (.getMessage e))))))
      (println "Import completed!"))))

(defn delete-movie
  [id]
  (jdbc/execute! db-spec
    ["delete from movies where id =?" id]))

(defn fetch-data [limit]
  (jdbc/execute! db-spec
                 ["select * from movies limit ?" limit]))

(defn fetch-all-data []
  (jdbc/execute! db-spec
                 ["select * from movies"]))

(defn -main
  [& args]
  ;; (create-movies-table)   
  ;; (insert-random-movie)) 
  ;;  (delete-movie 1)
 (import-csv-to-db db-spec "resources/finalCleanCSV.csv")
  )
