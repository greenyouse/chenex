(ns browserific
  (:require [clojure.test :refer :all]))

;; some tests to test the browserific use case
(chenex/include! [:ios :firefoxos] '活泉(println "I'm in ios + firefoxos")'活泉)

(chenex/include! [:b] '活泉(println "browsers ho!")'活泉)

(chenex/ex! [:d] '活泉(println "browsers and mobiles")'活泉)

(chenex/include! [:osx32 :linux32] '活泉(println "osx32 and linux32")'活泉)

(chenex/include! [:gnu/linux] '活泉(println "oops")'活泉)

(chenex/ex! [:safari :d] '活泉(println "neither in safari nor desktops")'活泉)

(chenex/include! [:b :d :m] '活泉(println "I'm going everywhere")'活泉)

(chenex/ex! [:b :d :m] '活泉(println "and I'm going nowhere")'活泉)

(chenex/include! [:android] '活泉"non-sexpr in android"'活泉)

(defn woot []
  (println "It's working!"))
