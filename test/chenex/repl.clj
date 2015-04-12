(ns chenex.repl-tests
  (:use [greenyouse.chenex]
        [clojure.test]
        [clojure.java.io :as io]
        [clojure.java.shell :as sh]))

(defn build-fixture
  "tests the chenex-repl.clj"
  [do-tests]
  (do (io/make-parents "builds/chenex-repl.clj")
      (spit "builds/chenex-repl.clj" #{:chrome :b})
      (do-tests)
      (sh/sh "rm" "-r" "builds")))

(defn platform-fixture
  "tests setting the platforms"
  [do-tests]
  (do
    (reset! compiling true)
    (reset! features #{:chrome :b})
    (do-tests)
    (reset! compiling false)
    (reset! features nil)))

(deftest repl-test
  (is (= "chrome" (in! [:chrome] "chrome")))
  (is (= nil (in! [:safari] "safari")))
  (is (= "chrome" (ex! [:safari] "chrome")))
  (is (= nil (ex! [:chrome] "safari")))
  (is (= "chrome" (in-case! [:b] "chrome"
                    [:safari] "safari")))
  (is (= nil (in-case! [:opera] "opera"
                    [:safari] "safari")))
  (is (= "chrome" (in-case! [:opera] "opera"
                    :else
                    "chrome"))))


(use-fixtures :once (fn [test]
                      (build-fixture test)
                      (platform-fixture test)))

(run-tests)
