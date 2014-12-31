(ns chenex.parser
  (:require [instaparse.core :as insta]
            [clojure.java.io :as io]))

(def ^:private fe-parser
  "This parser captures feature expressions and does inner transformations"
  (insta/parser "
PROGRAM = (FEATURE-EXPR* FLUFF*)* | SPACE
<FLUFF> = #'(?:(?!\\(chenex\\/include\\!|\\(chenex\\/ex\\!).|\\s*)*'

(* inlined the whitespaces + comments for speed *)
<SPACE> = #'^[\\s*,]*' | #'^(?=;|#!).*[^\\n]' SPACE*

(* Unidiomatic but makes the parser fast (^_^)/ ちぇええええええん！*)
<CODE> = #'(?:(?!\\'ﾊﾟﾌﾊﾟﾌ).|\\s*)*'

CHENEX-READER-LITERAL = 'chenex/include!' | 'chenex/ex!'
FEATURES = #'^\\[[^\\]]*(?:\\\\.[^\\]]*)*]'
CONTENT = <'\\'ﾊﾟﾌﾊﾟﾌ'> CODE <'\\'ﾊﾟﾌﾊﾟﾌ'>
FEATURE-EXPR = <'('> CHENEX-READER-LITERAL <SPACE>* FEATURES <SPACE>*
               CONTENT <SPACE>* <')'>
"))

(defn- do-transforms
  "Takes a vector of transform fns and adds them to a transformation"
  [coll]
  (apply comp (reverse coll)))

(defn- feature-transform [inclusive? listed-features content feature-set]
  "Gives the intersection of the feature-expr (listed-features) and
  the current feature rule (feature-set)"
  (let [features  (->> listed-features
                       (filterv #(feature-set %))
                       (set))]
    (cond
     ;; build included features
     (and inclusive? (some #(contains? features %) feature-set)) (str content "\n\n")
     ;; don't build negated features
     (and (not inclusive?) (seq feature-set)
          (seq listed-features) (empty? features)) (str content "\n\n")
     :else "")))

(defn- fe-transform [feature-set inner-transforms]
  {:CHENEX-READER-LITERAL (comp #(= "chenex/include!" %) str)
   :FEATURES (comp read-string str)
   :CONTENT (comp str)
   :FEATURE-EXPR (comp (do-transforms inner-transforms)
                       #(feature-transform % %2 %3 feature-set))
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
