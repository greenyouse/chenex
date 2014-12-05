(ns cljx
  (:require [clojure.test :refer :all]))

;; trying out basic clojure/clojurescript, cljx style
#+ [clj] (println "I'm a clojure file")

#- [cljs] (println "I'm a clojure file too")

#+ [cljs] (println "Nothing but clojurescript here")

#- [clj] (println "More cljs")

#+ [clj cljs] (println "This is in both!")

#- [cljs clj] (println "And this just disappears... be careful")

#+ [clj] ;not working yet, would be nice to save for readability though
(println "Clojure with comments saved")

#- [clj]
;; don't worry, spaces are fine
;; just make sure you have some sexprs
;; eventually
(println "woot woot it's clojurescript!")

(defn junk
  "Here is some junk to included in both"
  [x]
  (println (str "hi " x)))

(+ 1 1)
