(ns projekat.core-test
  (:require [clojure.test :refer :all]
            [projekat.cleaning :refer :all]
            [midje.sweet :refer [facts =>]]))

(facts "Parse-budget function test"
       (parse-budget "$200,000,000 (estimated)") => 200000000.0
       (parse-budget "₹1,500,000") => 18000.0
       (parse-budget "€100,000,000") => 106000000.0
       (parse-budget "¥10,000,000") => 66000.0
       (parse-budget "") => nil
       (parse-budget nil) => nil)