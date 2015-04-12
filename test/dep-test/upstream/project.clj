(defproject test.fake/upstream "0.1.0"
  :description "example of an upstream dep"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3126"]
                 [com.greenyouse/chenex "0.1.6"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [com.greenyouse/chenex "0.1.6"]]


  :profiles {:default [:base :system :user :provided :dev :plugin.chenex/default]}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["dev" "src"]
                        :compiler {:main upstream.core
                                   :output-to "resources/public/js/app.js"
                                   :output-dir "resources/public/js/out"
                                   :asset-path "js/out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "release"
                        :source-paths ["src"]
                        :compiler {:main upstream.core
                                   :output-to "resources/public/js/app.js"
                                   :optimizations :advanced
                                   :source-map "app.js.map"}}]})
