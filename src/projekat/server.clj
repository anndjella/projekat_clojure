(ns projekat.server
  (:require [clojure.edn :as edn]
            [projekat.lm :as lm]
            [ring.util.response :as resp]
            [ring.adapter.jetty :as jetty] 
            [ring.middleware.cors :refer [wrap-cors]]
            [cheshire.core :as json]))

(defn load-artifact []
  (edn/read-string (slurp "resources/lm-artifact.edn")))

(defonce artifact* (atom (load-artifact)))

(defn clamp 
  [x low high]
  (min high (max low x)))

(defn print-summary 
  "Prints detailed prediction breakdown"
  [stats zrow intercept selected-cols betas xs contribs sum-bx rating rating-clamped]
  (doseq [[c _] stats]
    (println (format "   %-40s z=% .6f" (name c) (double (get zrow c 0.0)))))
  (println (format "intercept = % .6f" (double intercept)))
  (println "Contributions (b*x):")
  (doseq [[nm b x bx] (map vector selected-cols betas xs contribs)]
    (println (format "   %-40s x=% .6f  b=% .6f  b*x=% .6f"
                     (name nm) x (double b) bx)))
  (println (format "sum b*x = % .6f" sum-bx))
  (println (format "rating  = intercept + sum = % .6f + % .6f = % .6f"
                   (double intercept) sum-bx rating))
  (println (format "rating  = % .6f" rating-clamped)))


(defn predict-movie-rating
  "Predicts movie rating based on input features and the linear model artifact"
  [artifact input]
  (let [{:keys [intercept betas selected-cols stats]} artifact
        zrow   (-> (lm/transform-row stats input)
                   (lm/add-interactions))
        xs     (mapv #(double (get zrow % 0.0)) selected-cols)

        contribs (mapv (fn [b x] (* (double b) (double x))) betas xs)
         sum-bx   (reduce + contribs)
        rating   (+ (double intercept) sum-bx)
        rating-clamped (clamp rating 1.0 10.0)]
    {:intercept intercept
     :betas betas
     :selected-cols selected-cols
     :stats stats
     :zrow zrow
     :xs xs
     :contribs contribs
     :sum-bx sum-bx
     :rating rating
     :rating-clamped rating-clamped}))

(defn predict-and-print
  "Predicts and prints detailed breakdown"
  [artifact input]
   (println ">> Parsed:" input) (flush)
  (let [{:keys [intercept betas selected-cols stats zrow xs contribs sum-bx rating rating-clamped]}
        (predict-movie-rating artifact input)]
    (print-summary stats zrow intercept selected-cols betas xs contribs sum-bx rating rating-clamped)
    rating-clamped))

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
      (try
        (let [a    @artifact*
              raw  (slurp (:body req))
              body (json/parse-string raw true)
               y   (predict-and-print a body)]
          (json-response {:prediction y}))
        (catch Exception e
          (json-response 400 {:error (.getMessage e)})))

      :else
      (-> (resp/response "Not found")
          (resp/status 404)
          (resp/header "Content-Type" "text/plain; charset=utf-8")))))


(def app
  (-> handler 
      (wrap-cors
       :access-control-allow-origin [#"http://localhost:8010" #".*"]
       :access-control-allow-methods [:get :post :options]
       :access-control-allow-headers ["Content-Type" "content-type"])))

(defn -main [& _]
  (println "Backend on http://localhost:3000")
  (jetty/run-jetty app {:port 3000 :join? false}))