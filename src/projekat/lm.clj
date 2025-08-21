(ns projekat.lm
  (:require [incanter.core :as i]
            [incanter.stats :as stats]
            [projekat.dbWork :as db]))

(def selected-cols 
  [ :num_of_ratings_cleaned :runtime_cleaned :drama :biography :war :history :documentary
    :animation :thriller :action :comedy :horror :release_year ])

(def target-col :rating_cleaned)

(defn unqualify-keys [row]
  (into {} (map (fn [[k v]] [(-> k name keyword) v]) row)))

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


(defn -main []
  (let [train (mapv unqualify-keys (db/fetch-all-data "movies_train"))
        test  (mapv unqualify-keys (db/fetch-all-data "movies_test"))
        model (train-linear-model train)
        y     (mapv target-col test)
        y-predicted  (predict-y model test)
        metrics  (evaluate y y-predicted)]
    (println "Metrics:" metrics)
    (println "Betas of model:")
    (println "Intercept:" (first (:coefs model)))
     (doseq [[k b] (map vector selected-cols (rest (:coefs model)))]
      (println (format "%-22s %.6f" (name k) (double b)))))


  
  )
