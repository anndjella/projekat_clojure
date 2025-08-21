(ns projekat.lm
  (:require [incanter.core :as i]
            [incanter.stats :as stats]
            [projekat.dbWork :as db]))

(def selected-cols 
  [ :runtime_cleaned :comedy ])

(def target-col :rating_cleaned)

(def train-rows [{:rating_cleaned 4.5 :runtime_cleaned 120, :horror 1, :comedy 0},
                  {:rating_cleaned 6 :runtime_cleaned 23, :horror 1, :comedy 1},
                  {:rating_cleaned 9.2 :runtime_cleaned 230, :horror 0, :comedy 0}
                 {:rating_cleaned 8.6 :runtime_cleaned 123, :horror 1, :comedy 0}])

;; (for [k selected-cols] (mapv k train-rows))
;; (def xcols(for [k selected-cols] (mapv k train-rows)))
;; (def xcols-transpose(apply mapv vector xcols))

;; (mapv (fn [r] (mapv (fn [k] (double (get r k))) selected-cols)) train-rows)

(defn train-linear-model
   [train-rows]
   (let [  y    (mapv target-col train-rows)
                  xcols (mapv (fn [r]
                                 (mapv (fn [k] (double (get r k))) selected-cols))
                               train-rows)
                   x-matrix    (i/matrix xcols)]
           (stats/linear-model y x-matrix)))

(defn -main []
  (train-linear-model train-rows))
