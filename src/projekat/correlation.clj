(ns projekat.correlation
  (:require [incanter.stats :as stats]
            ))
           
;; (defn correlations-to-rating
;;   ;;racuna korelaciju dve zadate kolone
;;   [data target-col col]
;;   (let [xs (mapv target-col data)
;;         ys (mapv col data)]
;;     (stats/correlation xs ys)))

;;  (correlations-to-rating [{:rating 1.0 :runtime 2.0}{:rating 2.0 :runtime 4.0} {:rating 3.0 :runtime 6.0}]
;;                          :rating :runtime) ;;korelacija runtime i rating 

(defn correlations-to-rating
  [data target-col]
  (let [cols (remove #{target-col :movies/id} (keys (first data)))]
   
          (map (fn [c] [c (stats/correlation (mapv target-col data) (mapv c data))]) cols)))


;; (correlations-to-rating [{:rating 1.0 :runtime 2.0} {:rating 2.0 :runtime 4.0} {:rating 3.0 :runtime 6.0}] :rating)

;; (remove #{:movies/rating_cleaned :movies/id}(keys (first (db/fetch-all-data))))

;; (mapv #{:movies/rating_cleaned} (keys (first (db/fetch-all-data))))
;; (first (db/fetch-all-data))

;; (mapv (fn [x] (* x 2)) [1 2 3])

;; (map (fn [c] [c (stats/correlation (mapv :rating [{:rating 1.0 :runtime 2.0} {:rating 2.0 :runtime 4.0} {:rating 3.0 :runtime 6.0}]) (mapv c [{:rating 1.0 :runtime 2.0} {:rating 2.0 :runtime 4.0} {:rating 3.0 :runtime 6.0}]))]) [:rating :runtime])


