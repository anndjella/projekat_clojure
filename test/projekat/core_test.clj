(ns projekat.core-test
  (:require [clojure.test :refer :all]
            [projekat.cleaning :refer :all]
            [projekat.imputation :refer :all]
            [projekat.correlation :refer :all]
            [midje.sweet :refer :all]
            [projekat.dbWork :refer :all]
            ))

(facts "Parse-budget function test"
       (parse-budget "$200,000,000 (estimated)") => 200000000.0
       (parse-budget "₹1,500,000") => 18000.0
       (parse-budget "€100,000,000") => 106000000.0
       (parse-budget "¥10,000,000") => 66000.0
       (parse-budget "") => nil
       (parse-budget nil) => nil)

(facts "Clean-budget tests"
       (clean-budget {:ime "andjela" :Budget "$1000,0" :prezime "Stan"} "Budget")
       => {:ime "andjela", :Budget "$1000,0", :prezime "Stan", :Budget-Cleaned 10000.0}
       
       (clean-budget {:ime "luka" :Budget "SEK2000,0" :prezime "asrsic"} "Budget")
       =>{:ime "luka", :Budget "SEK2000,0", :prezime "asrsic", :Budget-Cleaned 1820.0}
       
       (clean-budget {:ime "luka" :Gross-worldwide "SEK2000,0" :prezime "asrsic"} "Gross-worldwide")
       => {:ime "luka", :Gross-worldwide "SEK2000,0", :prezime "asrsic", :Gross-worldwide-Cleaned 1820.0}
       
        (clean-budget {:ime "slavca" :Gross-in-US-&-Canada "SEK2000,0" :prezime "kikic"} "Gross-in-US-&-Canada")
       => {:ime "slavca", :Gross-in-US-&-Canada "SEK2000,0", :prezime "kikic", :Gross-in-US-&-Canada-Cleaned 1820.0}
       
       (clean-budget {:ime "andjela" :Budget "" :prezime "Stan"} "Budget")
       => {:ime "andjela", :Budget "", :prezime "Stan", :Budget-Cleaned nil}
       
       (clean-budget {:ime "luka" :Budget "InvalidFormat" :prezime "asrsic"} "Budget")
       => {:ime "luka", :Budget "InvalidFormat", :prezime "asrsic", :Budget-Cleaned nil}
       
        (clean-budget {:ime "andjela" :prezime "Stan"} "Budget")
         => {:Budget-Cleaned nil :ime "andjela" :prezime "Stan"})

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

(facts "Parse-runtime fn tests"
       (parse-runtime "2h 12m") => 132
       (parse-runtime "1h 5m") => 65
       (parse-runtime "5m") => 5
       (parse-runtime "1h 5m") => 65
       (parse-runtime "2h") => 120
       (parse-runtime "2") => 0
       (parse-runtime "") => nil
       (parse-runtime nil) => nil)

(facts "Parse-num-of-ratings fn tests" 
       (parse-num-of-ratings "89M") => 89000000
       (parse-num-of-ratings "34K") => 34000
       (parse-num-of-ratings "233") => 233
       (parse-num-of-ratings " ") => nil
       (parse-num-of-ratings nil) => nil)

(facts "Parse-genres fn tests"
       (parse-genres {:random_key "1234 ":Main-Genres "Action,Adventure,Comedy"}) => ["Action" "Adventure" "Comedy"]
       (parse-genres {:Main-Genres "Action,Action,Adventure"}) => ["Action" "Adventure"] 
       (parse-genres {:Main-Genres "Action, ,Adventure"}) => ["Action" "Adventure"]
       (parse-genres {:Main-Genres " , , "}) => []
       (parse-genres {:Main-Genres ""}) => [] 
       (parse-genres {:Main-Genres nil}) => [])

(facts "Extract-distinct-genres function tests"
       (extract-distinct-genres [{:random_key "1234 " :Main-Genres "Action,Adventure,Comedy"},
                                 {:rfff "fdfd" :Main-Genres ""}]) => ["Action" "Adventure" "Comedy"]
       (extract-distinct-genres [{:random_key "1234 " :Main-Genres "Akcija,Drama,"},
                                 {:rfff "fdfd" :Main-Genres "Triler,Drama"}]) => ["Akcija" "Drama" "Triler"]
       (extract-distinct-genres [{:random_key "1234 " :Main-Genres ""},
                                 {:rfff "fdfd" :Main-Genres ""}]) => []
       (extract-distinct-genres [{:random_key "1234 " :Main-Genres ","},
                                 {:rfff "fdfd" :Main-Genres "Triler"}]) => [ "Triler"]
       )

(facts "Create-genre-map fn tests"
       (create-genre-map ["Action" "Komedija" "Triler"]) => {:Action 0, :Komedija 0, :Triler 0}
       (create-genre-map []) => {}
       (create-genre-map [ "Komedija"]) => { :Komedija 0}
       )

(facts "encode-genres fn tests"
       (encode-genres ["Triler" "Komedija" "Akcija"] ["Komedija"]) => {:Triler 0, :Akcija 0, :Komedija 1}
       (encode-genres ["Akcija" "Romansa"] [])=> {:Akcija 0 :Romansa 0} )

(facts "Add-genre-columns functiob tests"
       (add-genre-columns {:prva "a" :druga "v" :Main-Genres "Akcija,Romansa"} ["Akcija" "Romansa"])
       => {:prva "a" :druga "v" :Main-Genres "Akcija,Romansa" :Akcija 1 :Romansa 1}
       (add-genre-columns {:prva "a" :druga "v" :Main-Genres "Akcija,Romansa"} ["Akcija" "Romansa" "Triler"])
       => {:prva "a" :druga "v" :Main-Genres "Akcija,Romansa" :Akcija 1 :Romansa 1 :Triler 0}
       (add-genre-columns {:prva "a" :druga "v" :Main-Genres ""} ["Akcija" "Romansa"])
       => {:prva "a" :druga "v" :Main-Genres "" :Akcija 0 :Romansa 0}
       )
;;;;;;imputation tests;;;;;

(facts "Parse-value tests"
       (parse-value "42") => 42.0
       (parse-value "3.14") => 3.14
       (parse-value "2.5E3") => 2500.0
       (parse-value "1.2e-2") => 0.012
       (parse-value nil) => nil
       (parse-value "") => nil
       (parse-value "   ") => nil
       (parse-value "Hello") => "Hello" 
       (parse-value "2023-07-05") => "2023-07-05")

(facts "is-empty tests"
       (is-empty? "") => true
       (is-empty? " ") => true
       (is-empty? "d") => false
       (is-empty? nil) => true)

(facts "calculate-mean tests"
        (calculate-mean [{:col1 "10"} {:col1 "20"} {:col1 "30"}] :col1) => 20.0
       (calculate-mean [{:col2 "10.25"} {:col2 "20.75"} {:col2 "30.5"}] :col2) => 20.5
       (calculate-mean [{:Release-Year "1999"} {:Release-Year "2001"} {:Release-Year "2003"}] :Release-Year) => 2001
       (calculate-mean [{:col3 "10"} {:col3 nil} {:col3 ""} {:col3 "20"} {:col3 "30"}] :col3) => 20.0 
       (calculate-mean [{:col4 nil} {:col4 ""} {:col4 nil}] :col4) => nil)

(facts "fill-missing tests"
       (let [rows [{:rating 3 :Budget-Cleaned 30} {:rating 10 :Budget-Cleaned nil} {:rating 7 :Budget-Cleaned 25}]
             expected [{:rating 3 :Budget-Cleaned 30} {:rating 10 :Budget-Cleaned 27.5} {:rating 7 :Budget-Cleaned 25}]] 
         (fill-missing rows :Budget-Cleaned) => expected)
       
       (let [rows [{:rating 3 :Budget-Cleaned nil} {:rating 10 :Budget-Cleaned nil} {:rating 7 :Budget-Cleaned nil}]
             expected [{:rating 3 :Budget-Cleaned nil} {:rating 10 :Budget-Cleaned nil} {:rating 7 :Budget-Cleaned nil}]]
         (fill-missing rows :Budget-Cleaned) => expected)
       
       (let [rows [{:rating 3 :Budget-Cleaned 30} {:rating 10 :Budget-Cleaned 667} {:rating 7 :Budget-Cleaned 2545}]
             expected [{:rating 3 :Budget-Cleaned 30} {:rating 10 :Budget-Cleaned 667} {:rating 7 :Budget-Cleaned 2545}]]
         (fill-missing rows :Budget-Cleaned) => expected))

;;===============corr tests======================
(facts "correlations-to-rating tests"
       (correlations-to-rating [{:rating 1.0 :runtime 2.0}
                                 {:rating 2.0 :runtime 4.0}
                                {:rating 3.0 :runtime 6.0}]
                                :rating) => {:runtime 1.0}

       (correlations-to-rating [{:rating 1.0 :runtime 6.0}
                                 {:rating 2.0 :runtime 4.0}
                                {:rating 3.0 :runtime 2.0}]
                               :rating)  => {:runtime -1.0}
       
       (correlations-to-rating [{:rating 1.0 :runtime 6.0}
                                {:rating 2.0 :runtime nil}
                                {:rating 3.0 :runtime 2.0}]
                               :rating) => (throws Exception))


(facts "multicollinear-pairs tests"

       (multicollinear-pairs
        [{:movies/id 1 :x 1.0 :y 2.0 :z 8.0 :movies/rating_cleaned 10.0}
         {:movies/id 2 :x 2.0 :y 4.0 :z 6.0 :movies/rating_cleaned  9.0}
         {:movies/id 3 :x 3.0 :y 6.0 :z 4.0 :movies/rating_cleaned  8.0}
         {:movies/id 4 :x 4.0 :y 8.0 :z 2.0 :movies/rating_cleaned  7.0}]
        0.9)
       => (contains
           [(contains :x :y (roughly  1.0 1e-12))
            (contains :x :z (roughly -1.0 1e-12))
            (contains :y :z (roughly -1.0 1e-12))]
           :in-any-order)

       (multicollinear-pairs
        [{:movies/id 1 :x 1.0 :y 2.0 :z 8.0 :movies/rating_cleaned 10.0}
         {:movies/id 2 :x 2.0 :y 4.0 :z 6.0 :movies/rating_cleaned  9.0}
         {:movies/id 3 :x 3.0 :y 6.0 :z 4.0 :movies/rating_cleaned  8.0}
         {:movies/id 4 :x 4.0 :y 8.0 :z 2.0 :movies/rating_cleaned  7.0}]
        1.0)
       => (contains
           [(contains :x :y (roughly  1.0 1e-12))
            (contains :x :z (roughly -1.0 1e-12))
            (contains :y :z (roughly -1.0 1e-12))]
           :in-any-order)

       (multicollinear-pairs
        [{:x 1.0 :y 2.0 :z 8.0}
         {:x 2.0 :y 4.0 :z 6.0}
         {:x 3.0 :y 6.0 :z 4.0}
         {:x 4.0 :y 8.0 :z 2.0}]
        0.5)
       => (contains
           [(contains :x :y (roughly  1.0 1e-12))
            (contains :x :z (roughly -1.0 1e-12))
            (contains :y :z (roughly -1.0 1e-12))]
           :in-any-order))

;;db tests
(facts  "split-train-test-rows tests"
 (split-train-test-rows [1 2 3 4 5 6 ] 1 23)=> (throws Exception)
 (split-train-test-rows [1 2 3 4 5 6] 0.2 23) => {:train [6] :test [3 5 1 4 2]}
 (split-train-test-rows [1 2 3 4 5 6] 0.8 23) => {:train [6 3 5 1 ] :test [4 2]}
 (split-train-test-rows [1 2 3 4 5 6] 2 23) => (throws Exception))
