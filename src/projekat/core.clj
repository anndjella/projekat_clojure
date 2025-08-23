(ns projekat.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [projekat.cleaning :as clean]
            [projekat.correlation :as corr]
            [projekat.imputation :as imputation]
            [projekat.lm :as lm]
            [projekat.dbWork :as db]))


 (defn generate-report
   []
   (let [{:keys [header rows]} (clean/process-data "resources/IMDbMovies.csv")]
     (println (str "Number of movies in this dataset: " (clean/number-of-rows rows)))
     (println "All attributes in the dataset:")
     (println (str/join ", " header))
     (clean/print-num-of-missing-values header rows)
     (println "===============================")
     (clean/ratio-NA header rows)
     (println "===============================")
     (println "We need to clean values so that we can handle NA values...\n")
     ;;(println (get-all-currency-prefixes rows))
     ;;(println (get-all-distinct-rated rows))
     ;;  (clean/process-and-save-data "resources/cleanedCSV.csv" header rows)
     (println "Values are successfully cleaned and put in cleanedCSV. 
                         Variable Main-Genres has been one-hot encoded.\n")
     ;; (println (clean/extract-distinct-genres rows)) 
     (println "We need to replace NA values with mean of corresponding column...\n")
     ;;(imputation/process-csv "resources/cleanedCSV.csv" "resources/finalCleanCSV.csv")
     (println "NA values are successfully replaced and put in finalCleanCSV.\n")
     (println "We need to decide which variables to include in analysis and for that 
                     we need to inspect their correlations with Rating variable...\n")
     (corr/analyze-correlation)
     ;; (corr/show-corr-chart)
     (println "===============================")
     (println "Kept the following features (correlation > |0.08|, except 'gross*' variables which were excluded due to frequent missing values):")
     (println (clojure.string/join ", "
                                   ["runtime_cleaned"
                                    "num_of_ratings_cleaned"
                                    "drama"
                                    "biography"
                                    "war"
                                    "history"
                                    "documentary"
                                    "animation"
                                    "thriller"
                                    "action"
                                    "comedy"
                                    "horror"
                                    "release_year"]))
     (println "===============================")
     (println "Check multicollinearity among selected predictors\n")
     (corr/print-multicollinearity 0.8)
     (println "\nThe only predictor pair with |r| > 0.8 is 
                 gross_worldwide_cleaned <> gross_in_us_canada_cleaned (r=0.9165). 
                 However, all 'gross*' variables were already excluded due to 
                 missing values, so we proceed with the remaining features"))
   (println "\nNext, we split the movies dataset into training (80%) and test (20%) datasets (movies_train, movies_test)")
  ;;  (db/insert-data-train-test 0.8 26)
   (println "After splitting the dataset, we can proceed to train and evaluate our model\n")
   (lm/train-eval)
   )

(defn -main
  [& arg]
  (generate-report)
  )

  



