(ns chenex.parser
  (:require [clojure.java.io :as io]
            [clojure.string :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Transforms

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

(defn- fe-transform
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
                       true (rest fe))
      chenex/ex-case! ((comp (do-transforms inner-transforms)
                             (case-transform feature-set))
                       false (rest fe)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Parser

(declare parse-nodes)

;; NOTE: must keep datatype intact, e.g. [] -> [], not to seq
(defn node-filter [t coll]
  (cond
    (and (list? coll)
      (re-find #"chenex" (str (first coll)))) (t coll)
      (sequential? coll) (cond
                           (empty? coll) coll
                           (vector? coll) (into [] (parse-nodes t coll))
                           :else
                           (parse-nodes t coll))
    :else
    coll))

(defn- parse-nodes
  "Recursively searches the entire tree for matches using a depth-first
  search. When a match is found, process fe, otherwise do nothing."
  ([transform exprs] (trampoline (parse-nodes transform exprs nil)))
  ([transform exprs acc]
   (if (empty? exprs) (reverse acc)
       #(parse-nodes transform (rest exprs)
          (conj acc (node-filter transform (first exprs)))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Interface

;; FIXME: `ns` will kill the parser if it's not in the first sexpr. Why?
(defn- prep
  "Helper fn that wraps code in a vector before passing it to parse-nodes."
  [feature-set inner-transforms expr]
  (let [t (fe-transform feature-set inner-transforms)
        ;; must wrap expr in a sequence to return all the nodes
        fe (->> expr str (str "[") s/reverse (str "]") s/reverse read-string)]
    (parse-nodes t fe)))

(defn- exit
  "Helper fn to convert back to string and unwrap the code."
  [fe]
  (-> fe str (subs 1) s/reverse (subs 1) s/reverse))

;; TODO: try getting outer transforms to work later. Second parser will
;;  be required.
;; TODO: would be nice to filter out the nils and pretty print the output
(defn start-parse [file-in file-out features inner-transforms
                   outer-transforms]
  (let [in-p (->> file-in
               slurp
               (prep features inner-transforms)
               exit)]
    (do (io/make-parents file-out)
        (spit file-out in-p))))
