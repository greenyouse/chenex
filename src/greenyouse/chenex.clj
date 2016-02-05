(ns greenyouse.chenex
  "REPL fns that also get expanded when clj/cljs code is compiled"
  (:require [greenyouse.chenex.parser :as p]
            [plugin-helpers.core :as h])
  (import java.io.File))

;; these expand any upstream code at clj/cljs compile time
(def opts (atom {:compiling false
                 :features nil}))

(defn- index-transforms
  "The transforms and their associated feature expressions"
  []
  (reduce #(assoc %
             (get-in %2 [:rules :features])
             (get-in %2 [:rules :inner-transforms]))
    {} (h/get-project-value :chenex :builds)))

(defn- get-features
  "Finds all the feature expressions. When compiling, it reads only the
  feature expressions in the build otherwise it reads the value
  from the :repl entry in project.clj."
  []
  (let [{:keys [compiling features]} @opts]
    (if compiling
      features
      ;; this avoids throwing a nil Exception
      (or (h/get-project-value :chenex :repl) #{}))))

(defn- get-transforms []
  (let [trans# (get (index-transforms) (get-features))]
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
