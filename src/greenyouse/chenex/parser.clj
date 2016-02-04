(ns greenyouse.chenex.parser
  (:require [clojure.java.io :as io]
            [rewrite-clj.zip :as z]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Transforms

;; TODO: do a general cleanup here later

;; TODO: could optimize transforms a little bit
(defn- do-transforms
  "Takes a vector of transform fns and adds them to a transformation"
  [coll]
  (apply comp (reverse coll)))

(defn- cond-transform
  "Gives the intersection of the feature-expr (listed-features) and
  the current feature rule (feature-set)"
  [feature-set]
  (fn [inclusive? listed-features content]
    (let [features  (->> listed-features
                      (filterv #(feature-set %))
                      (set))]
      (cond
        ;; build included features
        (and inclusive?
          (some #(contains? features %) feature-set)) content
          ;; don't build negated features
          (and (not inclusive?) (seq feature-set)
            (seq listed-features) (empty? features)) content))))

;; TODO: would be nice to report error location via (meta &form)
(defn- case-transform
  "Parses a case expression to find the correct value."
  [feature-set]
  (fn [inclusive? & clauses]
    (let [c (first clauses)
          else? (-> c reverse second keyword?) ;check for else clause
          conditions (if else?
                       (butlast (butlast c)) ;else found, reserve it
                       c)
          relevant (if inclusive? ;include only necessary platforms
                     (filter (fn [[plats expr]]
                               (some #(contains? feature-set %) plats))
                       (partition 2 conditions))
                     (partition 2 conditions))
          parsed-fe (reduce (fn [acc [plats expr]] ;process each platform
                              (conj acc
                                ((cond-transform feature-set)
                                 inclusive? plats expr)))
                      [] relevant)
          any-fe (filter #(not= nil %) parsed-fe)]
      (cond
        (< 1 (count any-fe)) (throw (Exception. (str "Chenex Error ambiguous expression found: " clauses)))
        (not (empty? any-fe)) (first any-fe) ;take the first plat from list
        (and (empty? any-fe) else?) (last c))))) ;return the else clause

(defn fe-transform
  "Invokes the transform fns on feature expressions."
  [feature-set inner-transforms]
  (fn [fe]
    (case (first fe)
      chenex/in! ((comp (do-transforms inner-transforms)
                            (cond-transform feature-set))
                       true (second fe) (first (nthrest fe 2)))
      chenex/ex! ((comp (do-transforms inner-transforms)
                        (cond-transform feature-set))
                  false (second fe) (first (nthrest fe 2)))
      chenex/in-case! ((comp (do-transforms inner-transforms)
                             (case-transform feature-set))
                       true (rest fe)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Parser

(def ^:private chenex-macros
  #{'chenex/in! 'chenex/ex! 'chenex/in-case!})

(defn- chenex-macro?
  "Returns true if the node is a chenex macro"
  [loc]
  (when (z/list? loc)
    (let [op (-> loc z/down z/value)]
      (boolean (chenex-macros op)))))

(defn- parse-nodes [xform forms]
  (z/prewalk
    (z/up forms) ; ensure all forms are walked
    chenex-macro?
    (fn [loc]
      (let [parsed (-> loc z/sexpr xform)]
        (if (nil? parsed)
          (z/remove loc) ; filter nodes not in the feature expression set
          (z/replace loc parsed))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Interface

(defn- prep
  "Helper fn that registers the transforms and calls the parser."
  [feature-set inner-transforms forms]
  (let [xform (fe-transform feature-set inner-transforms)]
    (parse-nodes xform forms)))

(defn start-parse [file-in file-out features inner-transforms]
  (let [in-p (->> file-in
                  z/of-file
                  (prep features inner-transforms)
                  z/root-string)]
    (do (io/make-parents file-out)
        (spit file-out in-p))))
