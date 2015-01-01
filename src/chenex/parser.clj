(ns chenex.parser
  (:require [instaparse.core :as insta]
            [clojure.java.io :as io]))

(def ^:private fe-parser
  "This parser captures feature expressions and does inner transformations"
  (insta/parser "
PROGRAM = (FEATURE-EXPR* FLUFF*)* | SPACE
<FLUFF> = #'(?:(?!\\(chenex\\/include\\!|\\(chenex\\/ex\\!|\\(chenex\\/in-case\\!|\\(chenex\\/ex-case\\!).|\\s*)*'

(* inlined the whitespaces + comments for speed *)
<SPACE> = #'^[\\s*,]*' | #'^(?=;|#!).*[^\\n]' SPACE*

(* Unidiomatic but makes the parser fast (^_^)/ *)
<CODE> = #'(?:(?!\\'ﾊﾟﾌﾊﾟﾌ).|\\s*)*'

FEATURES = #'^\\[[^\\]]*(?:\\\\.[^\\]]*)*]'
CONTENT = <'\\'ﾊﾟﾌﾊﾟﾌ'> CODE <'\\'ﾊﾟﾌﾊﾟﾌ'>
<CONDITION-CLAUSE> = FEATURES <SPACE>* CONTENT <SPACE>*
(* else clauses gets special attention in transformation, always at end *)
<ELSE-CLAUSE> = <':else'> <SPACE>* CONTENT
<CASE> = CONDITION-CLAUSE* [ELSE-CLAUSE]

CHENEX-CONDITION-MACRO = 'chenex/include!' | 'chenex/ex!'
CHENEX-CASE-MACRO = 'chenex/in-case!' | 'chenex/ex-case!'

CONDITION-EXPR = CHENEX-CONDITION-MACRO <SPACE>* CONDITION-CLAUSE
CASE-EXPR = CHENEX-CASE-MACRO <SPACE>* CASE <SPACE>*
<FEATURE-EXPR> = <'('> (CONDITION-EXPR | CASE-EXPR) <')'>
"))

(defn- do-transforms
  "Takes a vector of transform fns and adds them to a transformation"
  [coll]
  (apply comp (reverse coll)))

(defn- condition-transform
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
            (some #(contains? features %) feature-set)) (str content "\n\n")
       ;; don't build negated features
       (and (not inclusive?) (seq feature-set)
            (seq listed-features) (empty? features)) (str content "\n\n")
                 :else ""))))

(defn- case-transform
  "Parses a case expression to find the correct value."
  [feature-set]
  (fn [inclusive? & clauses]
    (let [conditions (if (-> clauses reverse second string?) ;checks for else clause
                       (butlast clauses) ;else found, reserve it
                       clauses)
          conditions (butlast clauses)
          valid-fe (loop [coll conditions acc ""]
                     (if (empty? coll)
                       acc
                       (recur (nthrest coll 2)
                              (str acc ((comp (condition-transform feature-set))
                                        inclusive? (first coll) (second coll))))))]
      (println valid-fe)
      (if (= "" valid-fe) ;if none of the clauses yielded anything
        (str (last clauses) "\n\n") ;return the else clause
        valid-fe))))

(defn- fe-transform [feature-set inner-transforms]
  {:CHENEX-CONDITION-MACRO (comp #(= "chenex/include!" %) str)
   :FEATURES (comp read-string str)
   :CONTENT (comp str)
   :CONDITION-EXPR (comp (do-transforms inner-transforms)
                         (condition-transform feature-set))
   :CHENEX-CASE-MACRO (comp #(= "chenex/in-case!" %) str)
   :CASE-EXPR (comp (do-transforms inner-transforms)
                    (case-transform feature-set))
   :PROGRAM (comp str)})


(def ^:private outer-parser
  "This parser is for outer transformations"
  (insta/parser "
PROGRAM = (SPACE SEXPR)* | SPACE

(* inlined the whitespaces + comments for speed *)
<SPACE> = #'^[\\s*,]*' | #'^(?=;|#!).*[^\\n]' SPACE*

<SEXPR> = (SEQ SPACE)* | (SYMS SPACE)*
SEQ = '(' (SYMS SEXPR)* ')' | '[' (SYMS SEXPR)* ']' | '{' (SYMS SEXPR)* '}' |
      '#{' (SYMS SEXPR)* '}'
<SYMS> =  #'(?:(?!\\(|\\)|\\[|\\]|\\{|\\}).|\\s*)*'

"))

(defn- outer-transform [outer-transforms]
  {:SEQ (comp (do-transforms outer-transforms))
   :PROGRAM (comp str)})

(defn start-parse [file-in file-out features inner-transforms outer-transforms]
  (let [in-trans (fe-transform features inner-transforms)
        in-p (-> file-in slurp fe-parser)
        in-t (insta/transform in-trans in-p)]
    (if (seq outer-transforms)
      (let [out-trans (outer-transform outer-transforms)
            out-t (->> in-t outer-parser (insta/transform out-trans))]
        (do (io/make-parents file-out)
            (spit file-out out-t)))
      (do (io/make-parents file-out)
          (spit file-out in-t)))))
