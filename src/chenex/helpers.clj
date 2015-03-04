(ns chenex.helpers)

(defmacro export
  "Little helper fn for clojurescirpt exports"
  [arg]
  `(with-meta arg {:export true}))
