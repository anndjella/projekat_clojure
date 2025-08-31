# Project goal

The goal of this project is **to predict a movie’s rating on IMDb**. IMDb is a website that collects user ratings for movies. Those ratings are subjective and depend on many factors.  
The model I use is **linear regression**. In short, it finds coefficients ($\beta$) so that a linear combination of inputs best fits the known ratings:

$$
{y} = \beta_0 + \sum_i \beta_i x_i
$$

The key point is that **all features must be numeric or 0/1** (one-hot encoded categories), so the linear model can work directly.

The data source is the **uncleaned Kaggle IMDbMovies.csv dataset**: <https://www.kaggle.com/datasets/elvinrustam/imdb-movies-dataset>

# Project flow and files

## `cleaning.clj`

1) **CSV analysis and column inspection.**  
2) **Cleaning the CSV** through the following steps:
   1. **Dropping columns** with too many NAs or that are hard to convert into numeric/categorical variables  
      (*Opening-Weekend-Gross-in-US-&-Canada, Summary, Director, Title, Writer, Motion-Picture-Rating*).
   2. **Currency normalization**: all monetary fields (*Budget, Gross-in-US-&-Canada, Gross-worldwide*) are converted to **USD** and parsed to numbers.
   3. **Rating**: strings like `"4.7/10"` are converted to `4.7` (double).
   4. **Runtime**: strings like `"2h12m"` are converted to **minutes** (e.g., `132`) (int).
   5. **Number of ratings**: formats like `"1.2M"` are converted to a plain **double** (`1200000`).
   6. **Genres (Main-Genres)**: converted to **one-hot encoding** — first, detect the set of all genres in the dataset; then, for each movie, set `1` for the genres it has and `0` for the rest.

`cleaning.clj` reads the original **IMDBMovies.csv** and the cleaned version is written as **cleanedCSV**.

---

## `imputation.clj`

For the cleaned data, where all columns are numeric or 0/1, **NA values** are filled with the **column mean**.  
From `cleanedCSV` I obtain **finalCleanCSV**.

---

## `dbWork.clj`

Enables importing the **entire finalCleanCSV** dataset into a **SQLite** database.  
The database is stored at **`resources/database.db`**.

In this file, the dataset is also split into train and test sets.

---

## `correlation.clj`

Used for **two purposes**:

1. **Pearson correlation** between the **dependent variable** (*rating*) and the **independent variables** (all the remaining ones after cleaning).  
   Decision: I keep in consideration all variables with **|r| ≥ 0.08**, where **r** is the *Pearson correlation coefficient*.  
   Exception: `gross_worldwide_cleaned` and `gross_in_us_canada_cleaned` - their correlations with *rating* isn't large and they showed **many NAs** early on, so they were **removed** for practical/data-quality reasons.
   ![alt text](resources/pictures/corr-plot.png)

2. **Correlation matrix of predictors (multicollinearity check)**:  
   For the remaining variables  
   *(runtime_cleaned, num_of_ratings_cleaned, drama, biography, war, history, documentary, animation, thriller, action, comedy, horror, release_year)*  
   there are **no pairs with |r| > 0.8**, so all were kept for modeling.

---


## License

Copyright © 2024 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
