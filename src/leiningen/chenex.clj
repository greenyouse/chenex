(ns leiningen.chenex
  (:require [leiningen.help :as lhelp]
            [leiningen.core.main :as lmain]
            [leiningen.core.eval :refer (eval-in-project)]
            [clojure.pprint :refer [pprint]]
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
  [temp]
  (lmain/info (yellow-text "Writing a new chenex configuration.\n"))
  (let [t# (first temp)
        loc# (try (-> (str "chenex/templates/" t# ".clj") io/resource slurp)
                  (catch Exception _ (lmain/abort (red-text (str "Chenex Error: template " (first temp) " not found.\n
Options are: cljx, browserific")))))]
    (do (io/make-parents "builds/chenex-build.clj")
        (spit "builds/chenex-build.clj" loc#))))

;; TODO: could improve this a bit more by adding multiple envs
(defn- repl
  "Sets a new target environment for the REPL iteractively. Enter the name
  of a chenex feature environement"
  [env]
  (lmain/info (yellow-text "Changing the chenex REPL.\n"))
  (let [e# (->> env first keyword list set)]
    (try (do (io/make-parents "builds/chenex-repl.clj")
             (spit "builds/chenex-repl.clj" (str e#)))
         (catch Exception _ (lmain/abort (red-text "Chenex Error: enter the name of a chenex feature\n\n"))))))

(defn chenex
  "Run the chenex compiler"
  {:help-arglists '([compile build repl])
   :subtasks [#'compile #'build #'repl]}
  ([project]
     (lmain/abort
      (lhelp/help-for "chenex")))
  ([project subtask & args]
     (case subtask
       "compile" (compile project)
       "build" (build args)
       "repl" (repl args)
       (lmain/abort (red-text (str "Chenex Error: Subtask " subtask " not found."))
                    (lhelp/subtask-help-for "chenex")))))
