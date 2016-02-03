(ns browserific
  (:require [greenyouse.chenex :as chenex]))

;; some code to show the browserific use case
(chenex/in! [:ios :firefoxos] (println "I'm in ios + firefoxos"))

(chenex/in! [:b] (println "browsers ho!"))

(chenex/ex! [:d] (println "browsers and mobiles"))

(chenex/in! [:osx32 :linux32] (println "osx32 and linux32"))

(chenex/in! [:gnu/linux] (println "oops"))

(chenex/ex! [:safari :d] (println "neither in safari nor desktops"))

(chenex/in! [:b :d :m] (println "I'm going everywhere"))

(chenex/ex! [:b :d :m] (println "and I'm going nowhere"))

(chenex/in! [:android] "non-sexpr in android")

(defn woot []
  (println "It's working!"))

;; now some compound tests
(chenex/in-case! [:firefox]  (println "either in firefox")
                 [:d]  (println "or desktops")
                 [:m]  (println "or mobile")
                 :else  (println "this goes wherever else"))
