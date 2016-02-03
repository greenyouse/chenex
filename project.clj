(defproject com.greenyouse/chenex "0.2.2-SNAPSHOT"
  :description "Another feature expression library for Clojure"
  :url "https://github.com/greenyouse/chenex"
  :license {:name "BSD 2-Clause"
            :url "http://www.opensource.org/licenses/BSD-2-Clause"}

  :dependencies [[org.clojure/clojure "1.6.0" :scope "provided"]
                 [org.clojure/clojurescript "0.0-3119" :scope "provided"]
                 [com.greenyouse/plugin-helpers "0.1.2"]]

  :dev {:dependencies [[org.clojure/test.check "0.7.0"]]}

  :eval-in-leiningen true)
