(ns greenyouse.chenex)

(defmacro in!
  "Outputs code for the specified platforms. Used at the REPL"
  [platforms body]
  (let [chenex-env# (-> "builds/chenex-repl.clj" slurp read-string)]
    `(if (seq (filter #(~chenex-env# %) ~platforms))
       ~body)))

(defmacro ex!
  "Outputs code for all the platforms availabile except the ones specified.
  Used at the REPL"
  [platforms body]
  (let [chenex-env# (-> "builds/chenex-repl.clj" slurp read-string)]
    `(let [features# (filterv #(~chenex-env# %) ~platforms)]
       (if (and (seq ~platforms) (empty? features#))
         ~body))))

(defn- include-clauses [coll]
  (->> coll
       (partition 2)
       (map (fn [expr]
              (in! (first expr) (second expr))))
       (filterv (complement nil?))))

;; This is a bit bigger than clojure's cond+case because it must detect
;; ambigous clauses. For example, including a platform twice in the same
;; inclusive statement should cause an error, not just return the first
;; valid clause.

(defn- parse-case [clauses]
  (let [else?#  (-> clauses reverse second keyword?) ;check for else clause
        conditions# (if else?#
                      ;;pull out else clause
                      (-> clauses butlast butlast)
                      clauses)
        valid-fe# (include-clauses conditions#)]
    (case (count valid-fe#)
      0 (if else?# (last clauses)) ;else or nil
      1 (first valid-fe#)
      (throw (Exception. "Chenex Error: ambiguous expressions found")))))

(defmacro in-case!
  "Syntactic sugar to have case-like in! statements."
  [& coll]
  (parse-case coll))
