{:default {:dependencies [[com.greenyouse/chenex "0.1.0"]
                          [com.cemerick/piggieback "0.1.3"]]
           :plugins [[lein-auto "0.1.1"]]
           :repl-options {:nrepl-middleware [chenex.repl/wrap-chenex
                                             cemerick.piggieback/wrap-cljs-repl]}}}
