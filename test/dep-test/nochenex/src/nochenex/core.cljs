(ns nochenex.core
  (:require [upstream.core :as up]))

;; Projects that don't use chenex but use a chenex library in their
;; dependencies will get wonky behavior. The build/chenex-repl.clj will
;; be used for expanding the upstream chenex code at compile time. If that
;; file doesn't exist (and it shouldn't) an error will be thrown.
;;
;; Solution: Just require chenex even if you're not going to write feature
;; expressions in your own code. This way you'll be able to target the
;; environments that the upstream code is set to target (which is the whole
;; point of depending on a library written with chenex).

(up/example "no chenex")
