(ns projekat.bench
  (:require [criterium.core :as crit]
            [projekat.dbWork :as db]
            [projekat.correlation :as corr]
            [projekat.config :as cfg]))
(defn -main [& _]
  (let [data (vec (db/fetch-all-data "movies"))]
    ;; =============corr/correlations-to-target=============

    ;;(crit/bench (corr/correlations-to-target data cfg/target-col cfg/feature-columns))

    ; Evaluation count : 300 in 60 samples of 5 calls.
    ;              Execution time mean : 203.053769 ms
    ;     Execution time std-deviation : 10.287597 ms
    ;    Execution time lower quantile : 196.741892 ms ( 2.5%)
    ;    Execution time upper quantile : 225.887661 ms (97.5%)
    ;                    Overhead used : 12.458469 ns

    ;;After improvement
    (crit/bench (corr/correlations-to-target data cfg/target-col cfg/feature-columns))
    ; Evaluation count : 1380 in 60 samples of 23 calls.
    ;              Execution time mean : 51.496042 ms
    ;     Execution time std-deviation : 5.258600 ms
    ;    Execution time lower quantile : 45.500580 ms ( 2.5%)
    ;    Execution time upper quantile : 62.259118 ms (97.5%)
    ;                    Overhead used : 8.977870 ns
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

