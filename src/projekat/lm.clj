(ns projekat.lm
  (:require [incanter.core :as i]
            [incanter.stats :as stats]
            [projekat.dbWork :as db]))

;; (def selected-cols 
;;   [ :num_of_ratings_cleaned :runtime_cleaned :drama :biography :war :history :documentary
;;     :animation :thriller :action :comedy :horror :release_year ])
(def selected-cols
  [:num_of_ratings_cleaned :runtime_cleaned :drama :biography :war :documentary
   :animation  :action :comedy :horror :release_year])

(def target-col :rating_cleaned)

(def numeric-cols [:num_of_ratings_cleaned :runtime_cleaned :release_year])

(def log-before-std?
  {:num_of_ratings_cleaned true
   :runtime_cleaned        false
   :release_year           false})


(defn fit-stats [train-rows]
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


(defn transform-row [stats row]
  (reduce (fn [acc c]
            (let [{:keys [mu sd log?]} (stats c)
                  x (double (get acc c 0.0))
                  x (if log? (Math/log1p x) x)]
              (assoc acc c (/ (- x mu) sd))))
          row numeric-cols))

(defn fit-preprocess [train-rows]
  (let [stats (fit-stats train-rows)]
    {:transform-row #(transform-row stats %)}))

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
  [train0 test0]
  (let [{:keys [transform-row]} (fit-preprocess train0)
        train (mapv transform-row train0)
        test  (mapv transform-row test0)
  
        model (train-linear-model train)
        y     (mapv target-col test)
        y-predicted  (predict-y model test)
        metrics  (evaluate y y-predicted)
        names (into [:intercept] selected-cols)
        rows  (map vector names (:coefs model) (:t-tests model) (:t-probs model))]
    (println "Metrics:" metrics)
  
    (println "Intercept:" (first (:coefs model)))
    ;;  (doseq [[k b] (map vector selected-cols (rest (:coefs model)))]
    ;;   (println (format "%-22s %.6f" (name k) (double b))))
    (println "\nSignificance (sorted by p desc):")
    (doseq [[nm b t p] (->> rows (drop 1) (sort-by (fn [[_ _ _ p]] p) >))]
      (println (format "%-22s b=% .6f  t=% .3f  p=%.4f%s"
                       (name nm) (double b) (double t) (double p)
                       (if (> (double p) 0.05) "   <-- kandidat za brisanje" ""))))))