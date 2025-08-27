(ns projekat.server
  (:require [clojure.edn :as edn]
            [projekat.lm :as lm]))

(defn load-artifact []
  (edn/read-string (slurp "resources/lm-artifact.edn")))

(defonce artifact* (load-artifact))

(defn predict-one [artifact input]
  (let [{:keys [intercept betas selected-cols stats]} artifact 
        zrow   (-> (lm/transform-row stats input)
                   (lm/add-interactions))
        xs     (mapv #(double (get zrow % 0.0)) selected-cols)
        sum-bx (reduce + (map * betas xs))
        rating   (+ (double intercept) sum-bx)]
    {:prediction     rating}))