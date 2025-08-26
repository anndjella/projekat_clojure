(ns projekat.config)

(def all-predictors
  [:sport :western :fantasy :family :animation :music :horror
   :mystery :musical :romance :num_of_ratings_cleaned :runtime_cleaned
   :war :gross_worldwide_cleaned :history :drama :sci_fi
   :gross_in_us_canada_cleaned :documentary :release_year :budget_cleaned 
   :adventure :comedy :thriller :action :film_noir :biography :crime]
)

(def feature-columns
  [ :num_of_ratings_cleaned :runtime_cleaned :drama :biography :war :history :documentary
   :animation :thriller :action :comedy :horror :release_year])

(def target-col :rating_cleaned)

(def final-feature-columns
  [:num_of_ratings_cleaned :runtime_cleaned :drama :biography  :documentary
   :animation :action :comedy :horror :release_year :num_of_ratings_cleaned_x_release_year])