(ns browserific
  (:require [clojure.test :refer :all]))

;; some tests to test the browserific use case
#+ [ios firefoxos] (println "I'm in ios + firefoxos")

#+ [b] (println "browsers ho!")

#- [d] (println "browsers and mobiles")

#+ [osx32 linux32] (println "osx32 and linux32")

#+ [gnu/linux] (println "oops")

#- [safari d] (println "neither in safari nor desktops")

#+ [b d m] (println "I'm going everywhere")

#- [b d m] (println "and I'm going nowhere")

#+ [android] "non-sexpr in android"

(defn woot []
  (println "It's working!"))
