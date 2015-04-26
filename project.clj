(defproject com.greenyouse/chenex "0.2.0-SNAPSHOT"
  :description "Another feature expression library for Clojure"
  :url "http://github.com/greenyouse/chenex"
  :license {:name "BSD 2-Clause"
            :url "http://www.opensource.org/licenses/BSD-2-Clause"}

  :dependencies [[org.clojure/clojure "1.6.0" :scope "provided"]
                 [org.clojure/clojurescript "0.0-3119" :scope "provided"]]

  :dev {:dependencies [[org.clojure/test.check "0.7.0"]]}

  :eval-in-leiningen true)
