(defproject com.greenyouse/chenex "0.2.2-SNAPSHOT"
  :description "Another feature expression library for Clojure"
  :url "https://github.com/greenyouse/chenex"
  :license {:name "BSD 2-Clause"
            :url "http://www.opensource.org/licenses/BSD-2-Clause"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [com.greenyouse/plugin-helpers "0.1.3"]
                 [rewrite-clj "0.4.12"]]

  :dev {:dependencies [[org.clojure/test.check "0.9.0"]]}

  :chenex {:builds [{:source-paths ["test/samples"]
                     :output-path "target/chenex"
                     :rules {:filetype "cljx"
                             :features #{:m}
                             :inner-transforms []}}]
           :repl #{:chrome
 :b}}

  :eval-in-leiningen true)
