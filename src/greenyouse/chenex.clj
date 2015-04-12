(ns greenyouse.chenex
  "REPL fns that also get expanded when clj/cljs code is compiled"
  (:require [greenyouse.chenex.parser :as p])
  (import java.io.File))

;; these expand any upstream code at clj/cljs compile time
(def compiling (atom false))

(def features (atom nil))


(def ^:private builds
  "The chenex builds for the current project"
  (if (.exists (File. "builds/chenex-builds.clj"))
    (-> "builds/chenex-builds.clj" slurp read-string)
    (as-> "project.clj" p
      (slurp p)
      (read-string p)
      (nthrest p 3)
      (apply hash-map p)
      (get-in p [:chenex :builds]))))

(def ^:private transforms
  "The transforms and their associated feature expressions"
  (reduce #(assoc %
             (get-in %2 [:rules :features])
             (get-in %2 [:rules :inner-transforms]))
    {} builds))

(defn- get-features
  "Finds all the feature expressions. When compiling, it reads only the
  feature expressions in #'features else it reads from builds/chenex-repl.clj
  (for normal repl development)."
  []
  (if @compiling
    @features
    (-> "builds/chenex-repl.clj" slurp read-string)))

(defn- get-transforms []
  (let [trans# (get transforms (get-features))]
    (if (empty? trans#) [] trans#)))

(defn parse-fe
  "Parses through one feature expression"
  [features transforms fe]
  (let [t# (p/fe-transform features transforms)]
    (t# fe)))

(defmacro in!
  "Outputs code for the specified platforms."
  [platforms body]
  (parse-fe (get-features) (get-transforms) `(chenex/in! ~platforms ~body)))

(defmacro ex!
  "Outputs code for all the platforms availabile except the ones specified."
  [platforms body]
  (parse-fe (get-features) (get-transforms) `(chenex/ex! ~platforms ~body)))

(defmacro in-case!
  "Syntactic sugar to have case-like in! statements."
  [& coll]
  (parse-fe (get-features) (get-transforms) `(chenex/in-case! ~@coll)))
