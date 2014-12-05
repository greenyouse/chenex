(ns leiningen.chenex
  (:require [leiningen.help :as lhelp]
            [leiningen.core.main :as lmain]
            [leiningen.core.eval :refer (eval-in-project)]
            [leiningen.core.project :as p]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [compile]))

(defn- yellow-text [msg] (str "\033[33m" msg "\033[0m"))
(defn- red-text [msg] (str "\033[31m" msg "\033[0m"))

(defn- compile
  "Compiles cljx files into new formats"
  [project]
  (let [log# (if (false? (get-in project [:chenex :log]))
               false true)]
    (if log#
      (lmain/info (yellow-text "Compiling files for chenex\n")))
    (if-let [builds# (get-in project [:chenex :builds])]
      (eval-in-project project
                       `(do (#'chenex.core/run '~builds#)
                            ~(when (-> project :eval-in name (= "subprocess"))
                               '(shutdown-agents)))
                       '(require 'chenex.core))
      (if log#
        (lmain/info (red-text "Chenex Error: No builds detected in project.clj"))))))

;;happy to add more templates here
(defn- build
  "Writes a basic build template, options are: cljx, browserific"
  [project temp]
  (lmain/info (yellow-text "Writing a new chenex configuration.\n"))
  (let [t# (first temp)
        loc# (try (-> (str "chenex/templates/" t# ".clj") io/resource slurp)
                  (catch Exception _ (lmain/abort (red-text (str "Chenex Error: template " (first temp) " not found.\n
Options are: cljx, browserific")))))]
    (do (io/make-parents "builds/chenex-builds.clj")
        (spit "builds/chenex-build.clj" loc#))))

(defn chenex
  "Run the chenex compiler"
  {:help-arglists '([compile build])
   :subtasks [#'compile #'build]}
  ([project]
     (lmain/abort
      (lhelp/help-for "chenex")))
  ([project subtask & args]
     (case subtask
       "compile" (compile project)
       "build" (build project args)
       (lmain/abort (red-text (str "Chenex Error: Subtask " subtask " not found."))
                    (lhelp/subtask-help-for "chenex")))))
