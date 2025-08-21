(ns projekat.correlation
  (:require [incanter.stats :as stats]
            [projekat.dbWork :as db]
            [incanter.charts :as charts]
            [incanter.core :refer [view]]
            )
  (:import  (org.jfree.chart.axis CategoryLabelPositions)))
           
(defn correlations-to-rating
  "Calculates Pearson correlations between a target column and all other columns in the dataset"
  [data target-col]
  (let [cols (remove #{target-col :movies/id} (keys (first data)))
        target-vec (mapv target-col data)] 
    
         (into {} (map (fn [c] [c (stats/correlation target-vec (mapv c data))]) cols))))

(defn print-correlations
  [correlations]
  (doseq [[k v] (sort-by val > correlations)] (printf "%s: %.4f\n" (name k) v)))

;; (print-correlations {:runtime 0.85 :budget -0.12})

(defn analyze-correlation
  []
  (let [data (db/fetch-all-data "movies")]
    (println "\nCorrelations with rating_cleaned:")
    (-> data
        (correlations-to-rating :movies/rating_cleaned) 
        (print-correlations))))

(def corrs (correlations-to-rating (db/fetch-all-data "movies") :movies/rating_cleaned))


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



(correlations-to-rating  (db/fetch-all-data "movies") :movies/rating_cleaned)
;; (correlations-to-rating [{:rating 1.0 :runtime 2.0} {:rating 2.0 :runtime 4.0} {:rating 3.0 :runtime 6.0}] :rating)

;; (remove #{:movies/rating_cleaned :movies/id}(keys (first (db/fetch-all-data))))

;; (mapv #{:movies/rating_cleaned} (keys (first (db/fetch-all-data))))
;; (first (db/fetch-all-data))

;; (mapv (fn [x] (* x 2)) [1 2 3])

;; (map (fn [c] [c (stats/correlation (mapv :rating [{:rating 1.0 :runtime 2.0} {:rating 2.0 :runtime 4.0} {:rating 3.0 :runtime 6.0}]) (mapv c [{:rating 1.0 :runtime 2.0} {:rating 2.0 :runtime 4.0} {:rating 3.0 :runtime 6.0}]))]) [:rating :runtime])



(defn multicollinear-pairs
  [data threshold]
  (let [cols (vec (remove #{:movies/rating_cleaned :movies/id} (keys (first data))))
        n    (count cols)]
    (->> (for [i (range n), j (range (inc i) n)]
           (let [xi (mapv (nth cols i) data)
                 xj (mapv (nth cols j) data)
                 r  (stats/correlation xi xj)]
             [(nth cols i) (nth cols j) r]))
         (filter #(>= (Math/abs (nth % 2)) threshold)))))

;; (multicollinear-pairs
;;  [{:movies/id 1 :x 1.0 :y 2.0 :z 8.0 :movies/rating_cleaned 10.0}
;;   {:movies/id 2 :x 2.0 :y 4.0 :z 6.0 :movies/rating_cleaned  9.0}
;;   {:movies/id 3 :x 3.0 :y 6.0 :z 4.0 :movies/rating_cleaned  8.0}
;;   {:movies/id 4 :x 4.0 :y 8.0 :z 2.0 :movies/rating_cleaned  7.0}]
;;  0.9)

(defn print-multicollinearity
  ([threshold]
   (doseq [[a b r] (multicollinear-pairs (db/fetch-all-data "movies") threshold)]
     (println (format "%s <> %s : r=%.4f" (name a) (name b) r)))))

;; (print-multicollinearity 0.8)