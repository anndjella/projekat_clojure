(ns projekat.bench
  (:require [criterium.core :as crit]
            [projekat.dbWork :as db]
            [projekat.correlation :as corr]))

(defn -main [& _]
  (let [data (vec (db/fetch-all-data "movies"))]
    ;; (crit/bench (corr/correlations-to-rating data :rating_cleaned))

    ; Evaluation count : 300 in 60 samples of 5 calls.
    ;              Execution time mean : 203.053769 ms
    ;     Execution time std-deviation : 10.287597 ms
    ;    Execution time lower quantile : 196.741892 ms ( 2.5%)
    ;    Execution time upper quantile : 225.887661 ms (97.5%)
    ;                    Overhead used : 12.458469 ns
    
    ;;After improvement
    (crit/bench (corr/correlations-to-rating data :rating_cleaned))
    ; Evaluation count : 660 in 60 samples of 11 calls.
    ;              Execution time mean : 100.015396 ms
    ;     Execution time std-deviation : 5.829793 ms
    ;    Execution time lower quantile : 96.107857 ms ( 2.5%)
    ;    Execution time upper quantile : 110.144036 ms (97.5%)
    ;     
    ))