(ns cljx
  (:require [chenex.macros :as chenex]))

;; trying out basic clojure/clojurescript, cljx style
(chenex/include [:clj] 'ﾊﾟﾌﾊﾟﾌ(println "I'm a clojure file")'ﾊﾟﾌﾊﾟﾌ)

(chenex/ex! [:cljs] 'ﾊﾟﾌﾊﾟﾌ(println "I'm a clojure file too")'ﾊﾟﾌﾊﾟﾌ)

(chenex/include [:cljs] 'ﾊﾟﾌﾊﾟﾌ(println "Nothing but clojurescript here")'ﾊﾟﾌﾊﾟﾌ)

(chenex/ex! [:clj] 'ﾊﾟﾌﾊﾟﾌ(println "More cljs")'ﾊﾟﾌﾊﾟﾌ)

(chenex/include [:clj :cljs] 'ﾊﾟﾌﾊﾟﾌ(println "This is in both!")'ﾊﾟﾌﾊﾟﾌ)

(chenex/ex! [:cljs :clj] 'ﾊﾟﾌﾊﾟﾌ(println "And this just disappears... be careful")'ﾊﾟﾌﾊﾟﾌ)

(chenex/include [:clj] 'ﾊﾟﾌﾊﾟﾌ ;save your junk comments too
                (println "Clojure with comments saved")'ﾊﾟﾌﾊﾟﾌ)

(chenex/ex! [:clj] 'ﾊﾟﾌﾊﾟﾌ
            ;; don't worry, spaces are fine
            ;; just make sure you have some sexprs
            ;; eventually
            (println "woot woot it's clojurescript!")'ﾊﾟﾌﾊﾟﾌ)

(defn junk
  "Here is some junk to included in both"
  [x]
  (println (str "hi " x)))
