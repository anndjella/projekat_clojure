(ns projekat.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn load-csv [filename]
  "Loads csv"
  (with-open [reader (io/reader filename)]
    (doall (csv/read-csv reader))))

(def data 
  "Loads data from csv file IMDBMovies.csv"
  (load-csv "resources/IMDbMovies.csv"))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
  





