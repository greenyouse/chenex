(ns cljx
  (:require [chenex.macros :as chenex]))

;; doing out basic clojure/clojurescript, cljx style
(chenex/include [:clj] (println "I'm a clojure file"))

(chenex/ex! [:cljs] (println "I'm a clojure file too"))

(chenex/include [:cljs] (println "Nothing but clojurescript here"))

(chenex/ex! [:clj] (println "More cljs"))

(chenex/include [:clj :cljs] (println "This is in both!"))

(chenex/ex! [:cljs :clj] (println "And this just disappears... be careful"))

(chenex/include [:clj]
                (println "Clojure with comments saved"))

(chenex/ex! [:clj]
            ;; don't worry, spaces are fine
            ;; just make sure you have some sexprs
            ;; eventually
            (println "woot woot it's clojurescript!"))

(defn junk
  "Here is some junk to included in both"
  [x]
  (println (str "hi " x)))
