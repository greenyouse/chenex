(defproject test.fake/nochenex "0.1.0"
  :description "A downstream project without chenex"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3126"]
                 [test.fake/upstream "0.1.0"]]

  :plugins [[lein-cljsbuild "1.0.5"]]

  :profiles {:dev {:dependencies [[weasel "0.6.0"]]}}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["dev" "src"]
                        :compiler {:main nochenex.core
                                   :output-to "resources/public/js/app.js"
                                   :output-dir "resources/public/js/out"
                                   :asset-path "js/out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "release"
                        :source-paths ["src"]
                        :compiler {:main nochenex.core
                                   :output-to "resources/public/js/app.js"
                                   :optimizations :advanced
                                   :source-map "app.js.map"}}]})
