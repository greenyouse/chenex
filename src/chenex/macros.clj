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
  (let [chenex-env (-> "builds/chenex-repl.clj" slurp read-string)
        features (filterv #(chenex-env %) platforms)]
    `(if (and (seq ~platforms) (empty? ~features))
       ~body)))
