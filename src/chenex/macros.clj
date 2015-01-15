(ns chenex.macros)

(defmacro include!
  "Outputs code for the specified platforms. Used at the REPL"
  [platforms _ body _]
  (let [chenex-env (-> "builds/chenex-repl.clj" slurp read-string)]
    `(if (seq (filter #(~chenex-env %) ~platforms))
       ~body)))

(defmacro ex!
  "Outputs code for all the platforms availabile except the ones specified.
  Used at the REPL"
  [platforms _ body _]
  (let [chenex-env (-> "builds/chenex-repl.clj" slurp read-string)]
    `(let [features# (filterv #(~chenex-env %) ~platforms)]
       (if (and (seq ~platforms) (empty? features#))
         ~body))))

(defn- include-clauses
  [coll]
  (->> coll
       (partition 4)
       (map (fn [expr]
              (include! (first expr) _ (nth expr 2) _))) ;obviously change this!
       (filterv (complement nil?))))

(defn- exclude-clauses
  [coll]
  (->> coll
       (partition 4)
       (map (fn [expr]
              (ex! (first expr) _ (nth expr 2) _))) ;obviously change this!
       (filterv (complement nil?))))

;; This is a bit bigger than clojure's cond+case because it must detect
;; ambigous clauses. For example, including a platform twice in the same
;; inclusive statement should cause an error, not just return the first
;; valid clause. Similarly, having more than two clauses in exclusive that
;; are not members of the feature set will create an ambiguity error. The
;; latter is goofy and renders most compoud, exclusive statements useless.
;; Watch out!

(defn- parse-case [inclusive? clauses]
  (let [else? (if (keyword? (nth (reverse clauses) 3)) ;else clause detected
                 true false)
        conditions (if else?
                      (-> clauses ;pull out else clause
                          reverse
                          (nthrest 4)
                          reverse)
                      clauses)
        valid-fe# (if inclusive? (include-clauses conditions#)
                      (exclude-clauses conditions#))]
    (case (count valid-fe)
      0 (if else? (-> clauses reverse (nth 1))) ;else or nil
      1 (first valid-fe)
      (throw (Exception. "Chenex Error: ambiguous expressions found")))))

(defmacro in-case!
  "Syntactic sugar to have case-like include! statements."
  [& coll]
  (parse-case true coll))

(defmacro ex-case!
  "Syntactic sugar over ex! to allow for case-like statements. Be careful
  not to include multiple valid expressions."
  [& coll]
  (parse-case false coll))
