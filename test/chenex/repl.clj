(ns chenex.repl-tests
  (:require [clojure.java.io :as io]
            [plugin-helpers.core :as h])
  (:use [clojure.test]
        [greenyouse.chenex]))

;; TODO: make a tear down for the entire :chenex entry,
;;  add a h/remove-in-project for deleting kv pairs
(defn build-fixture
  "tests the chenex-repl.clj"
  [do-tests]
  (let [orig (h/get-project-value :chenex :repl)]
    (h/assoc-in-project [:chenex :repl] #{:chrome :b})
    (do-tests)
    (h/assoc-in-project [:chenex :repl] orig)))

(defn platform-fixture
  "tests setting the platforms"
  [do-tests]
  (do
    (reset! opts {:compiling true
                  :features #{:chrome :b}})
    (do-tests)
    (reset! opts {:compiling false
                  :features nil})))

(deftest repl-test
  (are [expected expr]
      (= expected expr)
    "chrome" (in! [:chrome] "chrome")
    nil (in! [:safari] "safari")
    "chrome" (ex! [:safari] "chrome")
    nil (ex! [:chrome] "safari")
    "chrome" (in-case! [:b] "chrome"
               [:safari] "safari")
    nil (in-case! [:opera] "opera"
          [:safari] "safari")
    "chrome" (in-case! [:opera] "opera"
               :else
               "chrome")))


(use-fixtures :once (fn [test]
                      (build-fixture test)
                      (platform-fixture test)))

(comment (run-tests))
