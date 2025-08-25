(ns projekat.correlation
  (:require [incanter.stats :as stats]
            [projekat.dbWork :as db]
            [projekat.config :as cfg]
            [incanter.charts :as charts]
            [incanter.core :refer [view]])
  (:import  (org.jfree.chart.axis CategoryLabelPositions)))
           
(defn correlations-to-target
  "Calculates correlations between a target column and each column in cols"
  [data target-col cols]
  (let [target-vec (mapv target-col data)]
    (into {}
          (pmap (fn [c] [c (stats/correlation target-vec (mapv #(get % c) data))]) cols))))

(defn print-correlations
  "Pretty-prints correlations sorted descending by r"
  [correlations]
  (doseq [[k v] (sort-by val > correlations)] (printf "%s: %.4f\n" (name k) v)))

(defn analyze-correlation
  "Convenience helper: prints header and then the sorted correlations to target-col"
  [data cols target-col]
 (println (format "\nCorrelations with %s:" (name target-col)))
  (-> data
        (correlations-to-target target-col cols) 
        (print-correlations)))

(def corrs (correlations-to-target (db/fetch-all-data "movies") cfg/target-col cfg/feature-columns))


(defn show-corr-chart 
  "Renders a bar chart of correlations"
  []
  (let [sorted (sort-by val > corrs)   
      labels (map (comp name key) sorted)
      values (map val sorted)
      chart  (charts/bar-chart labels values
                               :x-label "Columns"
                               :y-label "Correlation with rating_cleaned")]
  (-> chart .getCategoryPlot .getDomainAxis
      (.setCategoryLabelPositions CategoryLabelPositions/UP_45))
  (view chart :width 1400 :height 800)))


(defn multicollinear-pairs
  "Finds multicollinearity among predictors whose absolute correlation is ≥ threshold"
  [data threshold cols]
  (let [cols   (vec cols)
        col-v (into {} (map (fn [c] [c (mapv #(double (get % c)) data)]) cols))
        n      (count cols)]
    (->> (for [i (range n), j (range (inc i) n)]
           (let [ci (nth cols i)
                 cj (nth cols j)
                 r  (stats/correlation (col-v ci) (col-v cj))]
             (when (>= (Math/abs r) threshold) [ci cj r])))
         (keep identity)
         vec)))

(defn print-multicollinearity
  "Prints each qualifying pair from multicollinear-pairs"
  ([data threshold cols]
   (doseq [[a b r] (multicollinear-pairs data threshold cols)]
     (println (format "%s <> %s : r=%.4f" (name a) (name b) r)))))
