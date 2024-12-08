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

(facts "Clean-budget tests"
       (clean-budget {:ime "andjela" :Budget "$1000,0" :prezime "Stan"})
       => {:ime "andjela", :Budget "$1000,0", :prezime "Stan", :Budget-Cleaned 10000.0}
       
       (clean-budget {:ime "luka" :Budget "SEK2000,0" :prezime "asrsic"})
       =>{:ime "luka", :Budget "SEK2000,0", :prezime "asrsic", :Budget-Cleaned 1820.0} )

(facts "Map-row fn tests"
       (map-row {:Ime "andjela" :Prezime "stan" :godiste "2001"} [:Ime :Prezime :godiste])
       => ["andjela" "stan" "2001"])

(facts "Parse-rating fn tests"
       (parse-rating "4.5/10") => 4.5
       (parse-rating "6.0/10") => 6.0
       (parse-rating "") => nil
       (parse-rating nil) => nil)

(facts "Clean-rating tests"
       (clean-rating {:ime "andjela" :Rating "3.2/10" :prezime "Stan"})
       => {:ime "andjela", :Rating "3.2/10", :prezime "Stan", :Rating-Cleaned 3.2}

       (clean-rating {:ime "luka" :Rating nil :prezime "asrsic"})
       => {:ime "luka", :Rating nil, :prezime "asrsic", :Rating-Cleaned nil})