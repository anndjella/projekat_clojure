(ns projekat.lm
  (:require [incanter.core :as i]
            [incanter.stats :as stats]))


(def numeric-cols [:num_of_ratings_cleaned :runtime_cleaned :release_year])

(def log-before-std?
  {:num_of_ratings_cleaned true
   :runtime_cleaned        false
   :release_year           false})


(defn fit-stats
  "For each numeric column, computes the mean and sample standard deviation (after optional log1p) and records whether log1p was used"
  [train-rows]
  (into {}
        (for [c numeric-cols
              :let [lb?  (log-before-std? c)
                    vals (mapv #(double (get % c 0.0)) train-rows)
                    xs   (if lb? (mapv #(Math/log1p %) vals) vals)
                    n    (count xs)
                    mu   (if (pos? n) (stats/mean xs) 0.0)
                    sd0  (if (> n 1)  (double (stats/sd xs)) 0.0) ; sample SD (n-1)
                    sd   (if (pos? sd0) sd0 1.0)]]
          [c {:mu mu :sd sd :log? lb?}])))


(defn transform-row 
  "Applies optional log1p and z-score standardization to a single row"
  [stats row]
  (reduce (fn [acc c]
            (let [{:keys [mu sd log?]} (stats c)
                  x (double (get acc c 0.0))
                  x (if log? (Math/log1p x) x)]
              (assoc acc c (/ (- x mu) sd))))
          row numeric-cols))

(defn fit-preprocess
   "Returns a row transformer fitted on the training data"
  [train-rows]
  (let [stats (fit-stats train-rows)]
    {:transform-row #(transform-row stats %)}))

(defn add-interactions
  "Add interaction to a row"
  [row]
  (let [num   (double (get row :num_of_ratings_cleaned 0.0))
        rel (double (get row :release_year 0.0))]
    (assoc row :num_of_ratings_cleaned_x_release_year (* num rel))))

(defn train-linear-model
  "Trains an linear model on the given feature columns"
   [train-rows target-col feature-cols]
   (let [  y    (mapv target-col train-rows)
                  xcols (mapv (fn [r]
                                 (mapv (fn [k] (double (get r k))) feature-cols))
                               train-rows)
                   x-matrix    (i/matrix xcols)]
           (stats/linear-model y x-matrix)))

(defn predict-y
  "Predicts target values for rows using the trained model"
  [model rows feature-cols]
  (let [coefs     (:coefs model)             
        intercept (first coefs)
        betas     (vec (rest coefs))]
    (mapv (fn [r]
            (let [x (mapv (fn [k] (double (get r k 0.0))) feature-cols)]
              (+ (double intercept) (reduce + (map * betas x)))))
          rows)))

(defn evaluate
  "Computes RMSE, MAE, and R² between true and predicted targets"
  [y-test y-predicted]
  (let [n (count y-test)
        ybar (/ (reduce + y-test) n)
         abs-errors (map (fn [a b] (Math/abs (double (- (double a) (double b)))))
                        y-test y-predicted)
         mae  (/ (reduce + abs-errors) n)
        ssres (reduce + (map (fn [a b] (let [e (- a b)] (* e e))) y-test y-predicted))
        sstot (reduce + (map (fn [a] (let [d (- a ybar)] (* d d))) y-test))]
    {:rmse (Math/sqrt (/ ssres n))
     :mae mae
     :r2   (if (zero? sstot)
             0.0
             (- 1.0 (/ ssres sstot)))}))

(defn train-eval
  "Prepares the data, trains the model, evaluates on the test set, and prints metrics."
  [train0 test0 target-col feature-cols]
  (let [{:keys [transform-row]} (fit-preprocess train0)
      train-z (mapv transform-row train0)
      test-z  (mapv transform-row test0)
      
      train   (mapv add-interactions train-z)
      test    (mapv add-interactions test-z)
  
        model (train-linear-model train target-col feature-cols)
        y     (mapv target-col test)
        y-predicted  (predict-y model test feature-cols)
        metrics  (evaluate y y-predicted)
        names (into [:intercept] feature-cols)
        rows  (map vector names (:coefs model) (:t-tests model) (:t-probs model))]
    (println "Metrics:" metrics)
  
    (println "Intercept:" (first (:coefs model)))
    ;;  (doseq [[k b] (map vector selected-cols (rest (:coefs model)))]
    ;;   (println (format "%-22s %.6f" (name k) (double b))))
    (println "\nSignificance (sorted by p desc):")
    (doseq [[nm b t p] (->> rows (drop 1) (sort-by (fn [[_ _ _ p]] p) >))]
      (println (format "%-40s b=% .6f  t=% .3f  p=%.4f%s"
                       (name nm) (double b) (double t) (double p)
                       (if (> (double p) 0.05) "   <-- kandidat za brisanje" ""))))))