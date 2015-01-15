(ns browserific
  (:require [chenex.macros :as chenex]))

;; some code to show the browserific use case
(chenex/include! [:ios :firefoxos] (println "I'm in ios + firefoxos"))

(chenex/include! [:b] (println "browsers ho!"))

(chenex/ex! [:d] (println "browsers and mobiles"))

(chenex/include! [:osx32 :linux32] (println "osx32 and linux32"))

(chenex/include! [:gnu/linux] (println "oops"))

(chenex/ex! [:safari :d] (println "neither in safari nor desktops"))

(chenex/include! [:b :d :m] (println "I'm going everywhere"))

(chenex/ex! [:b :d :m] (println "and I'm going nowhere"))

(chenex/include! [:android] "non-sexpr in android")

(defn woot []
  (println "It's working!"))

;; now some compound tests
(chenex/in-case! [:firefox]  (println "either in firefox")
                 [:d]  (println "or desktops")
                 [:m]  (println "or mobile")
                 :else  (println "this goes wherever else"))

;; this is the most common use for ex-case!
(chenex/ex-case! [:firefox]  (println "everywhere not firefox")
                 :else  (println "here's code for firefox") )

(chenex/ex-case! [:m :d]  (println "everywhere not mobile")
                 [:firefox]  (println "this throws an error at the REPL, see why?")
                 :else  (println "here's code for mobile + desktop") )
