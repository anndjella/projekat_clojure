(ns projekat.lm
  (:require [incanter.core :as i]
            [incanter.stats :as stats]
               [clojure.pprint :as pp] 
            [projekat.dbWork :as db]))

(def selected-cols 
  [ :runtime_cleaned :comedy ])

(def target-col :rating_cleaned)

(def train-rows [{:rating_cleaned 4.5 :runtime_cleaned 120, :horror 1, :comedy 0},
                  {:rating_cleaned 6 :runtime_cleaned 23, :horror 1, :comedy 1},
                  {:rating_cleaned 9.2 :runtime_cleaned 230, :horror 0, :comedy 0}
                 {:rating_cleaned 8.6 :runtime_cleaned 123, :horror 1, :comedy 0}])

(def test-rows [{:rating_cleaned 9.7 :runtime_cleaned 68, :horror 1, :comedy 1},
                 {:rating_cleaned 7.5 :runtime_cleaned 98, :horror 1, :comedy 0}
                 ])

(defn train-linear-model
   [train-rows]
   (let [  y    (mapv target-col train-rows)
                  xcols (mapv (fn [r]
                                 (mapv (fn [k] (double (get r k))) selected-cols))
                               train-rows)
                   x-matrix    (i/matrix xcols)]
           (stats/linear-model y x-matrix)))

(defn predict-y
  [model rows]
  (let [coefs     (:coefs model)             
        intercept (first coefs)
        betas     (vec (rest coefs))]
    (mapv (fn [r]
            (let [x (mapv (fn [k] (double (get r k 0.0))) selected-cols)]
              (+ (double intercept) (reduce + (map * betas x)))))
          rows)))

(defn evaluate
  [y-test y-predicted]
  (let [n (count y-test)
        ybar (/ (reduce + y-test) n)
        ssres (reduce + (map (fn [a b] (let [e (- a b)] (* e e))) y-test y-predicted))
        sstot (reduce + (map (fn [a] (let [d (- a ybar)] (* d d))) y-test))]
    {:rmse (Math/sqrt (/ ssres n))
     :r2   (- 1.0 (/ ssres sstot))}))

(def model (train-linear-model train-rows))
(predict-y model test-rows)

(def y-test     (mapv target-col test-rows))

(def predicted-y  (predict-y model test-rows))

(def eval-test  (evaluate y-test predicted-y))

(evaluate y-test predicted-y)
(defn -main []
  (train-linear-model train-rows)
(println "Test  metrics:" eval-test)



  )
