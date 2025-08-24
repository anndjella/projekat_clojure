(ns projekat.dbWork
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [projekat.imputation :as imputation]
   [next.jdbc.result-set :as rs]))

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

;;  (defn fetch-all-data
;;       [table-name]
;;      (jdbc/execute! db-spec [(str "SELECT * FROM " table-name)]))

(defn fetch-all-data
  [table-name]
  (jdbc/execute! db-spec
                 [(str "SELECT * FROM " table-name)]
                 {:builder-fn rs/as-unqualified-lower-maps}))

(def selected-cols
  "Column id is not a feature but it is useful to have ID in train and test datasets"
  [:id :rating_cleaned :num_of_ratings_cleaned :runtime_cleaned :drama :biography :war :history :documentary
   :animation :thriller :action :comedy :horror :release_year])

(defn create-empty-like
  "Creates empty table with same tipes as movies table, only for selected-cols."
  [conn target]
  (let [cols-info     (jdbc/execute! conn ["PRAGMA table_info(movies)"]
                                     {:builder-fn rs/as-unqualified-lower-maps})
        sel           (set selected-cols)
        selected-info (filter (comp sel keyword :name) cols-info)
        cols-ddl      (->> selected-info
                           (map (fn [{:keys [name type]}]
                                  (format "%s %s" name type)))
                           (str/join ", "))]
    (jdbc/execute! conn [(format "DROP TABLE IF EXISTS %s" target)])
    (jdbc/execute! conn [(format "CREATE TABLE %s (%s)" target cols-ddl)])))

(defn shuffle-with-seed
  "Shuffles a collection"
  ([coll seed]
   (let [alist (java.util.ArrayList. coll)
         rnd   (java.util.Random. (long seed))]
     (java.util.Collections/shuffle alist rnd)
     (vec alist))))

(defn split-train-test-rows
  "Splits collection to train  and test, using ratio (must be in (0,1)) for spliting and seed"
  [rows ratio seed]
  (when (or (not (number? ratio)) (<= ratio 0) (>= ratio 1))
    (throw (ex-info "ratio must be in (0,1)" {:ratio ratio})))
  (let [v (vec (shuffle-with-seed rows seed))
        k (int (* ratio (count v)))]
    {:train (subvec v 0 k)
     :test  (subvec v k)}))

(def selected-cols-sql
  (clojure.string/join ", " (map name selected-cols)))

(defn insert-data-train-test
  "Inserts data from Movies table to movies_train and movies_test"
  [ratio seed]
  (with-open [conn (jdbc/get-connection db-spec)]
    (jdbc/with-transaction [tx conn]
      (let [rows                  (vec (jdbc/execute! tx [(str "SELECT " selected-cols-sql " FROM movies")]
                                                      {:builder-fn rs/as-unqualified-lower-maps}))
            {:keys [train test]}  (split-train-test-rows rows ratio seed)
            row-vals             (apply juxt selected-cols)
            train-data            (mapv row-vals train)
            test-data             (mapv row-vals test)]
        (create-empty-like tx "movies_train")
        (create-empty-like tx "movies_test")
        (sql/insert-multi! tx "movies_train" selected-cols train-data)
        (sql/insert-multi! tx "movies_test"  selected-cols test-data)
        (let [msg (format "Successfully wrote to DB — Train: %d rows | Test: %d rows"
                          (count train) (count test))]
          (println msg)
          {:message msg :train-count (count train) :test-count (count test)})))))

(defn -main
  [& args]
  ;; (create-movies-table)   
  ;; (insert-random-movie)) 
  ;;  (delete-movie 1)
  ;; (import-csv-to-db db-spec "resources/finalCleanCSV.csv")
  )