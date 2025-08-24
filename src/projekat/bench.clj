(ns projekat.bench
  (:require [criterium.core :as crit]
            [projekat.dbWork :as db]
            [projekat.correlation :as corr]
            [projekat.config :as cfg]))
(defn -main [& _]
  (let [data (vec (db/fetch-all-data "movies"))]
    ;; =============corr/correlations-to-rating=============

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
    ; =============corr/print-multicollinearity=============

    ;;  (crit/bench
    ;;  (binding [*out* (java.io.StringWriter.)]
    ;;    (corr/print-multicollinearity data 0.8)))
  ; Evaluation count : 60 in 60 samples of 1 calls.
;              Execution time mean : 5.687586 sec
;     Execution time std-deviation : 303.625410 ms
;    Execution time lower quantile : 5.388505 sec ( 2.5%)
;    Execution time upper quantile : 6.369662 sec (97.5%)
;                    Overhead used : 10.399420 ns
    (crit/bench
   (binding [*out* (java.io.StringWriter.)]
     (corr/print-multicollinearity data 0.8 cfg/feature-columns)))
     ; Evaluation count : 180 in 60 samples of 3 calls.
;              Execution time mean : 432.367882 ms
;     Execution time std-deviation : 32.930956 ms
;    Execution time lower quantile : 397.138648 ms ( 2.5%)
;    Execution time upper quantile : 535.085186 ms (97.5%)
;                    Overhead used : 10.399420 ns

    ))

