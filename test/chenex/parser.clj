(ns chenex.parser-tests
  (:require [rewrite-clj.zip :as z])
  (:use [greenyouse.chenex.parser]
        [clojure.test]))

;; TODO: write test.check generators for each chenex macro
;;  and validate that code survives a roundtrip with all
;;  feature expressions parsed out
;; TODO: Explain the case execution order in more detail in README

;;; feature expression permuatations:
;; when true and in expr -> first valid expr (true)
;; when true and not in expr -> else (fail)
;; when false and in expr -> else (fail)
;; when false and not in expr -> first valid expr (true)

(defn inner-trans1 [expr]
  (clojure.string/replace expr #"firefox" "Woot"))

(defn inner-trans2 [expr]
  (clojure.string/replace expr #"Woot" "looking good"))

(defmacro with-private-fns [[ns fns] & tests]
  "Refers private fns from ns and runs tests in context."
  `(let ~(reduce #(conj % %2 `(ns-resolve '~ns '~%2)) [] fns)
     ~@tests))

(deftest cond-transform-test
  (with-private-fns [greenyouse.chenex.parser [cond-transform]]
    (is (= ((cond-transform #{:chrome}) true [:chrome] '(+ 1 1)) '(+ 1 1)))
    (is (= ((cond-transform #{:chrome}) true [:firefox] '(+ 1 1)) nil))
    (is (= ((cond-transform #{:chrome}) false [:chrome] '(+ 1 1)) nil))
    (is (= ((cond-transform #{:chrome}) false [:firefox] '(+ 1 1)) '(+ 1 1)))))

(deftest case-transform-test
  (with-private-fns [greenyouse.chenex.parser [case-transform]]
    (is (= ((case-transform #{:chrome}) true '([:firefox] (+ 1 1)
                                               [:safari :chrome] (+ 2 2)
                                               :else "woot"))
          '(+ 2 2)))
    (is (= ((case-transform #{:chrome}) true '([:firefox] (+ 1 1)
                                               [:safari] (+ 2 2)
                                               :else "woot"))
          "woot"))
    (is (= ((case-transform #{:chrome}) false '([:chrome] (+ 1 1)
                                                :else "woot"))
          "woot"))
    (is (= ((case-transform #{:chrome}) false '([:firefox] (+ 1 1)
                                                :else "woot"))
          '(+ 1 1)))))

(comment
  (run-tests))

;; TODO: make a test for the samples?
(comment (start-parse "test/samples/browserific.cljx" "browserific.cljs" #{:firefox :b} [] []))

;; for testing the whole system (make this better)
(comment (with-private-fns [chenex.core [parse-src]]
            (parse-src "test/samples" "intermediate" "woot" #{:clj} [] [])))
