(ns chenex.parser-tests
  (:require [greenyouse.chenex.parser :refer :all]
            [clojure.test :refer :all]))


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

(deftest parser-test
  (with-private-fns [greenyouse.chenex.parser [prep exit]]
    ;; no feature exprs
    (is (= (read-string (exit (prep #{:chrome} []
                                 "(ns woot.content (:refer-clojure :exclude [atom])
  (:require [cljs.core.logic :as cl]
            [cljs.core.logic.pldb :as pl]
            [freactive.dom :as dom]
            [freactive.core :refer [atom cursor]]
            [cljs.core.async :as async :refer [put! <! chan]]
            [greenyouse.chenex :as chenex])
  (:require-macros [cljs.core.logic :as cm]
                   [cljs.core.logic.pldb :as pm]
                   [freactive.macros :refer [rx]]
                   [cljs.core.async.macros :refer [go-loop]]))")))
           (read-string
             "(ns woot.content (:refer-clojure :exclude [atom])
  (:require [cljs.core.logic :as cl]
            [cljs.core.logic.pldb :as pl]
            [freactive.dom :as dom]
            [freactive.core :refer [atom cursor]]
            [cljs.core.async :as async :refer [put! <! chan]]
            [greenyouse.chenex :as chenex])
  (:require-macros [cljs.core.logic :as cm]
                   [cljs.core.logic.pldb :as pm]
                   [freactive.macros :refer [rx]]
                   [cljs.core.async.macros :refer [go-loop]]))")))

    ;; testing one feature expr
    (is (= (read-string (exit (prep #{:firefox} []
                                 "(defn woot [hi]
  (let [p (chenex/in-case! [:chrome] \"chrome\"
            [:safari] \"safari\"
            [:firefox] \"firefox\"
            [:mobile] \"mobiles\"
            :else
            \"woot\")]
    (println (str \"Hello \" p))))")))
           (read-string "(defn woot [hi]
                   (let [p \"firefox\"]
                     (println (str \"Hello \" p))))")))

    ;; with one inner-transform
    (is (= (read-string (exit (prep #{:firefox} [inner-trans1]
                                "(defn woot [hi]
  (let [p (chenex/in-case! [:chrome] \"chrome\"
            [:safari] \"safari\"
            [:firefox] \"firefox\"
            [:mobile] \"mobiles\"
            :else
            \"woot\")]
    (println (str \"Hello \" p))))")))
          (read-string "(defn woot [hi]
                   (let [p \"Woot\"]
                     (println (str \"Hello \" p))))")))

    ;; with two inner-transforms
    (is (= (read-string (exit (prep #{:firefox} [inner-trans1 inner-trans2]
                                "(defn woot [hi]
  (let [p (chenex/in-case! [:chrome] \"chrome\"
            [:safari] \"safari\"
            [:firefox] \"firefox\"
            [:mobile] \"mobiles\"
            :else
            \"woot\")]
    (println (str \"Hello \" p))))")))
          (read-string "(defn woot [hi]
                   (let [p \"looking good\"]
                     (println (str \"Hello \" p))))")))))

(comment (run-tests))

;; TODO: make a test for the samples?
(comment (start-parse "test/samples/browserific.cljx" "browserific.cljs" #{:firefox :b} [] []))

;; for testing the whole system (make this better)
(comment (with-private-fns [chenex.core [parse-src]]
            (parse-src "test/samples" "intermediate" "woot" #{:clj} [] [])))
