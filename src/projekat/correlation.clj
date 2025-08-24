(ns projekat.correlation
  (:require [incanter.stats :as stats]
            [projekat.dbWork :as db]
            [projekat.config :as cfg]
            [incanter.charts :as charts]
            [incanter.core :refer [view]]
            )
  (:import  (org.jfree.chart.axis CategoryLabelPositions)))
           
;; (defn correlations-to-rating
;;   "Calculates Pearson correlations between a target column and all other columns in the dataset"
;;   [data target-col]
;;   (let [cols (remove #{target-col :movies/id} (keys (first data)))
;;         target-vec (mapv target-col data)] 
    
;;          (into {} (map (fn [c] [c (stats/correlation target-vec (mapv c data))]) cols))))

(defn correlations-to-rating [data target-col]
  (let [cols (remove #{target-col :id} (keys (first data)))
        target-vec (mapv target-col data)]
    (into {}
          (pmap (fn [c] [c (stats/correlation target-vec (mapv #(get % c) data))]) cols))))

(defn print-correlations
  [correlations]
  (doseq [[k v] (sort-by val > correlations)] (printf "%s: %.4f\n" (name k) v)))

;; (print-correlations {:runtime 0.85 :budget -0.12})

(defn analyze-correlation
  [data]
  (println "\nCorrelations with rating_cleaned:") 
  (-> data
        (correlations-to-rating :rating_cleaned) 
        (print-correlations)))

(def corrs (correlations-to-rating (db/fetch-all-data "movies") :rating_cleaned))


(defn show-corr-chart []
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

;; (multicollinear-pairs
;;  [{:movies/id 1 :x 1.0 :y 2.0 :z 8.0 :movies/rating_cleaned 10.0}
;;   {:movies/id 2 :x 2.0 :y 4.0 :z 6.0 :movies/rating_cleaned  9.0}
;;   {:movies/id 3 :x 3.0 :y 6.0 :z 4.0 :movies/rating_cleaned  8.0}
;;   {:movies/id 4 :x 4.0 :y 8.0 :z 2.0 :movies/rating_cleaned  7.0}]
;;  0.9)

(defn print-multicollinearity
  ([data threshold cols]
   (doseq [[a b r] (multicollinear-pairs data threshold cols)]
     (println (format "%s <> %s : r=%.4f" (name a) (name b) r)))))

;; (print-multicollinearity 0.8)