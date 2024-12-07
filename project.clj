(defproject projekat "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.csv "1.0.0"] 
                 [seesaw "1.5.0"]
                 [clj-http "3.12.3"]
                 [cheshire "5.10.0"]
                 [seancorfield/next.jdbc "1.1.613"]
                 [org.xerial/sqlite-jdbc "3.36.0.3"]
                 [midje "1.10.9"]]
  :main ^:skip-aot projekat.core
  ;; :main ^:skip-aot projekat.dbWork
   ;;:main ^:skip-aot projekat.cleaning
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :plugins [[lein-midje "3.2.2"]])
