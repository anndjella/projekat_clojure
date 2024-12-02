(ns projekat.dbWork
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [projekat.core :as core]))

(def db-spec
  {:dbtype "sqlite"
   :dbname "resources/database.db"})

(defn create-films-table []
  (jdbc/execute! db-spec
                 ["CREATE TABLE IF NOT EXISTS Movies (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title VARCHAR(255) NOT NULL,
                    summary TEXT,
                    director VARCHAR(255),
                    writer VARCHAR(255),
                    main_genres VARCHAR(255),
                    motion_picture_rating VARCHAR(10),
                    runtime VARCHAR(20),
                    release_year INT,
                    rating VARCHAR(10),
                    number_of_ratings INT,
                    budget VARCHAR(50),
                    gross_in_us_and_canada VARCHAR(50),
                    gross_worldwide VARCHAR(50),
                    opening_weekend_gross_in_us_and_canada VARCHAR(50),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                  )"]))


(defn insert-random-movie []
  (jdbc/execute! db-spec
                 ["INSERT INTO Movies (title, summary, director, writer, main_genres, motion_picture_rating, runtime, release_year, rating, number_of_ratings, budget, gross_in_us_and_canada, gross_worldwide, opening_weekend_gross_in_us_and_canada)
                   VALUES ('A Random Movie', 'A random movie with random values.', 'Random Director', 'Random Writer', 'Action,Adventure,Biography', 'R', '130', '2023', '7.5', 1500, '100000000', '150000000', '200000000', '50000000')"]))

(defn -main
  [& args]
  (create-films-table)   
  (insert-random-movie)) 
