(ns browserific
  (:require [chenex.macros :as chenex]))

;; some tests to test the browserific use case
(chenex/include! [:ios :firefoxos] 'ﾊﾟﾌﾊﾟﾌ(println "I'm in ios + firefoxos")'ﾊﾟﾌﾊﾟﾌ)

(chenex/include! [:b] 'ﾊﾟﾌﾊﾟﾌ(println "browsers ho!")'ﾊﾟﾌﾊﾟﾌ)

(chenex/ex! [:d] 'ﾊﾟﾌﾊﾟﾌ(println "browsers and mobiles")'ﾊﾟﾌﾊﾟﾌ)

(chenex/include! [:osx32 :linux32] 'ﾊﾟﾌﾊﾟﾌ(println "osx32 and linux32")'ﾊﾟﾌﾊﾟﾌ)

(chenex/include! [:gnu/linux] 'ﾊﾟﾌﾊﾟﾌ(println "oops")'ﾊﾟﾌﾊﾟﾌ)

(chenex/ex! [:safari :d] 'ﾊﾟﾌﾊﾟﾌ(println "neither in safari nor desktops")'ﾊﾟﾌﾊﾟﾌ)

(chenex/include! [:b :d :m] 'ﾊﾟﾌﾊﾟﾌ(println "I'm going everywhere")'ﾊﾟﾌﾊﾟﾌ)

(chenex/ex! [:b :d :m] 'ﾊﾟﾌﾊﾟﾌ(println "and I'm going nowhere")'ﾊﾟﾌﾊﾟﾌ)

(chenex/include! [:android] 'ﾊﾟﾌﾊﾟﾌ"non-sexpr in android"'ﾊﾟﾌﾊﾟﾌ)

(defn woot []
  (println "It's working!"))

;; now some compound tests
(chenex/in-case! [:firefox] 'ﾊﾟﾌﾊﾟﾌ (println "either in firefox")'ﾊﾟﾌﾊﾟﾌ
                 [:d] 'ﾊﾟﾌﾊﾟﾌ (println "or desktops")'ﾊﾟﾌﾊﾟﾌ
                 [:m] 'ﾊﾟﾌﾊﾟﾌ (println "or mobile")'ﾊﾟﾌﾊﾟﾌ
                 :else 'ﾊﾟﾌﾊﾟﾌ (println "this goes wherever else")'ﾊﾟﾌﾊﾟﾌ)

;; this is the most common use for ex-case!
(chenex/ex-case! [:firefox] 'ﾊﾟﾌﾊﾟﾌ (println "everywhere not firefox")'ﾊﾟﾌﾊﾟﾌ
                 :else 'ﾊﾟﾌﾊﾟﾌ (println "here's code for firefox")'ﾊﾟﾌﾊﾟﾌ )

(chenex/ex-case! [:m :d] 'ﾊﾟﾌﾊﾟﾌ (println "everywhere not mobile")'ﾊﾟﾌﾊﾟﾌ
                 [:firefox] 'ﾊﾟﾌﾊﾟﾌ (println "this throws an error, see why?") 'ﾊﾟﾌﾊﾟﾌ
                 :else 'ﾊﾟﾌﾊﾟﾌ (println "here's code for mobile + desktop")'ﾊﾟﾌﾊﾟﾌ )
