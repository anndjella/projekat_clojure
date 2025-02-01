(ns projekat.imputation
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
             [clojure.string :as str]))

(defn parse-value 
  [val]
  (cond
    (nil? val) nil
    (str/blank? val) nil
    (re-matches #"\d+(\.\d+)?([eE][-+]?\d+)?" val) (Double/parseDouble val)  
    :else val))
(defn load-csv
  [file]
  (with-open [reader (io/reader file)]
    (let [data (doall (csv/read-csv reader))
          header   (mapv #(if (.startsWith % ":")
                            (keyword (subs % 1))  
                            (keyword %)) (first data))   
          rows (mapv #(zipmap header (map parse-value %)) (rest data))]  
      {:header header
       :rows rows})))
(load-csv "resources/test.csv")
(defn is-empty? [value]
  (or (nil? value)
      (= value "")
      (and (string? value)
           (empty? (clojure.string/trim value)))))

(remove is-empty? {"nn" "ccc"})

(/ (Math/round ( * 3.888  10)) 10.0)

(defn calculate-mean 
  [rows column]
  (let [values (->> rows
                    (map #(get % column))
                    (remove is-empty?)
                    (map #(Double/parseDouble (str %))))]

    (when (seq values)
      (let [mean (/ (apply + values) (count values))]
        (if (= column :Release-Year)
          (Math/round mean)
        (-> mean
            (* 10)         
            (Math/round)   
            (/ 10.0)))))))


(map #(get % :budget) [{:budget "1111"}])
(calculate-mean [{:budget "1000" :dd 44} {:budget "111" }] :budget)
(calculate-mean [{:Release-Year "1000" :dd 44} {:Release-Year "111"}] :Release-Year)

(remove is-empty? (map #(Double/parseDouble (str %)) ["40","5"," "]))

(calculate-mean [{:Budget-Cleaned "1000000" :Rating-Cleaned "7.5"}
                 {:Budget-Cleaned "2000000" :Rating-Cleaned ""}
                 {:Budget-Cleaned "" :Rating-Cleaned "8.0"}
                 {:Budget-Cleaned "3000000" :Rating-Cleaned "9.0"}] 
                
                :Budget-Cleaned)


(defn fill-missing [rows column]
  (let [mean (calculate-mean rows column)]
    (mapv #(if (is-empty? (get % column))
             (assoc % column mean)
             %)
          rows)))

(fill-missing [{:Budget-Cleaned "1000000" :Rating-Cleaned "7.5"}
               {:Budget-Cleaned "2000000" :Rating-Cleaned ""}
               {:Budget-Cleaned "" :Rating-Cleaned "8.0"}
               {:Budget-Cleaned "3000000" :Rating-Cleaned "9.0"}]
              
              :Budget-Cleaned)

(reduce fill-missing [{:Budget-Cleaned "1000000" :Rating-Cleaned "7.5"}
                       {:Budget-Cleaned "2000000" :Rating-Cleaned ""}
                       {:Budget-Cleaned "" :Rating-Cleaned "8.0"}
                       {:Budget-Cleaned "3000000" :Rating-Cleaned "9.0"}]
                      
                      [:Budget-Cleaned :Rating-Cleaned])

(defn process-csv
  [input-file output-file]
  (let [{:keys [header rows]} (load-csv input-file)
        columns [:Budget-Cleaned
                 :Rating-Cleaned
                 :Runtime-Cleaned
                 :Release-Year
                 :Num-of-Ratings-Cleaned
                 :Gross-in-US-&-Canada-Cleaned
                 :Gross-worldwide-Cleaned]
        processed (reduce fill-missing rows columns)]
    (with-open [writer (io/writer output-file)]
      (csv/write-csv writer
                     (cons (mapv name header)
                           (mapv #(mapv (fn [h] (str (get % h))) header) processed))))))