(ns projekat.config)

(def feature-columns
  [ :num_of_ratings_cleaned :runtime_cleaned :drama :biography :war :history :documentary
   :animation :thriller :action :comedy :horror :release_year])

(def target-col :rating_cleaned)

(def final-feature-columns
  [:num_of_ratings_cleaned :runtime_cleaned :drama :biography  :documentary
   :animation :action :comedy :horror :release_year :num_of_ratings_cleaned_x_release_year])
