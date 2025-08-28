(ns projekat.server
  (:require [clojure.edn :as edn]
            [projekat.lm :as lm]
            [ring.util.response :as resp]
            [ring.adapter.jetty :as jetty]
            [cheshire.core :as json]))

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

(defn json-response
  ([m] (json-response 200 m))
  ([status m]
   (-> (resp/response (json/generate-string m))
       (resp/status status)
       (resp/header "Content-Type" "application/json; charset=utf-8"))))

(defn handler [req]
  (let [{:keys [request-method uri]} req]
    (cond 
      (and (= request-method :post) (= uri "/api/predict"))
      (let [raw  (slurp (:body req))
            body (json/parse-string raw true)]
        (json-response {:received body}))

      :else
      (-> (resp/response "Not found")
          (resp/status 404)
          (resp/header "Content-Type" "text/plain; charset=utf-8")))))


(def app handler)

(defn -main [& _]
  (println "Backend on http://localhost:3000")
  (jetty/run-jetty app {:port 3000 :join? false}))