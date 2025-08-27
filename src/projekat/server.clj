(ns projekat.server
  (:require [clojure.edn :as edn]))

(defn load-artifact []
  (edn/read-string (slurp "resources/lm-artifact.edn")))

(defonce artifact* (load-artifact))

(defn predict-one [artifact input]
  (let [{:keys [intercept betas selected-cols]} artifact
        xs     (mapv #(double (get input % 0.0)) selected-cols)
        sum-bx (reduce + (map * betas xs))
        rating   (+ (double intercept) sum-bx)]
    {:prediction     rating}))