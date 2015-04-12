(defproject test.fake/downstream "0.1.0"
  :description "Downstream project with chenex"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3126"]
                 [com.greenyouse/chenex "0.2.0"]
                 [test.fake/upstream "0.1.0"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [com.greenyouse/chenex "0.2.0"]]

  :profiles {:default [:base :system :user :provided :dev :plugin.chenex/default]
             :dev {:dependencies [[weasel "0.6.0"]]}}

  :chenex {:builds ~(-> "builds/chenex-builds.clj" slurp read-string)}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["dev" "target/generated-src/opera"]
                        :compiler {:main downstream.core
                                   :output-to "resources/public/js/app.js"
                                   :output-dir "resources/public/js/out"
                                   :asset-path "js/out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "release"
                        :source-paths ["target/generated-src/opera"]
                        :compiler {:main downstream.core
                                   :output-to "resources/public/js/app.js"
                                   :optimizations :advanced
                                   :source-map "app.js.map"}}]})
