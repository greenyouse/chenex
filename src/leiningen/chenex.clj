(ns leiningen.chenex
  (:require [leiningen.help :as lhelp]
            [leiningen.core.main :as lmain]
            [leiningen.core.eval :refer (eval-in-project)]
            [leiningen.core.project :as project]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [compile]))

(defn- yellow-text [msg] (str "\033[33m" msg "\033[0m"))
(defn- red-text [msg] (str "\033[31m" msg "\033[0m"))

(comment (defn- chenex-eip
  "Evaluates the given [form] within the context of a [project]. A single
  form that is to be run beforehand (for requires, etc) is specified by
  [init].
  This variant of eval-in-project implicitly adds the current :plugin dep on
  chenex to the main :dependencies vector of the project, as well as specifying
  that the eval should happen in-process in a new classloader (faster!)."
  [project init form]
  (eval-in-project
   (-> project
       (project/merge-profiles [{:dependencies [chenex.plugin/chenex-coordinates]}])
       ;; If the user has configured chenex to be run using :prep-tasks, this removes its
       ;; entry so eip doesn't try to circularly invoke chenex again.
       (update-in [:prep-tasks]
                  (partial remove #(or (= "chenex" (str %))
                                       (and (sequential? %)
                                            (= "chenex" (str (first %))))))))
   form
   init)))

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
