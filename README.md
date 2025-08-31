# Project goal

The goal of this project is **to predict a movie’s rating on IMDb**. IMDb is a website that collects user ratings for movies. Those ratings are subjective and depend on many factors.  
The model I use is **linear regression**. In short, it finds coefficients ($\beta$) so that a linear combination of inputs best fits the known ratings:

$$
{y} = \beta_0 + \sum_i \beta_i x_i
$$

The key point is that **all features must be numeric or 0/1** (one-hot encoded categories), so the linear model can work directly.

The data source is the **uncleaned Kaggle IMDbMovies.csv dataset**: <https://www.kaggle.com/datasets/elvinrustam/imdb-movies-dataset>

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
