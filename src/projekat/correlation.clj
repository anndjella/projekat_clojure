(ns projekat.correlation
  (:require [incanter.stats :as stats]
            [projekat.correlation :as corr]))
            
(defn correlations-to-rating
  [xs ys]
  (stats/correlation xs ys))

(correlations-to-rating [1 2 3] [2 4 6])