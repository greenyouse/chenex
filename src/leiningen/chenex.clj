(ns leiningen.chenex
  (:require [leiningen.help :as lhelp]
            [leiningen.core.main :as lmain]
            [leiningen.core.eval :refer (eval-in-project)]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh])
  (:refer-clojure :exclude [compile])
  (import java.io.File))

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
                       `(do (#'greenyouse.chenex.core/run '~builds#)
                            ~(when (-> project :eval-in name (= "subprocess"))
                               '(shutdown-agents)))
                       '(require 'greenyouse.chenex.core))
      (if log#
        (lmain/info (red-text "Chenex Error: No builds detected in project.clj"))))))


;;happy to add more templates here
(defn- build
  "Writes a basic build template, options are: cljx"
  [temp]
  (lmain/info (yellow-text "Writing a new chenex configuration.\n"))
  (let [t# (first temp)
        loc# (try (-> (str "greenyouse/chenex/templates/" t# ".clj") io/resource slurp)
                  (catch Exception _ (lmain/abort (red-text (str "Chenex Error: template " (first temp) " not found.\n
Options are: cljx")))))]
    (do (io/make-parents "builds/chenex-builds.clj")
        (spit "builds/chenex-builds.clj" loc#))))


(defn- repl
  "Sets a new target environment for the REPL iteractively. Enter the name
  of a chenex feature environement"
  [env]
  (lmain/info (yellow-text "Changing the chenex REPL.\n"))
  (let [e# (reduce #(conj % (keyword %2)) #{} env)]
    (try (do (io/make-parents "builds/chenex-repl.clj")
             (spit "builds/chenex-repl.clj" (str e#)))
         (catch Exception _ (lmain/abort (red-text "Chenex Error: enter the name of a chenex feature\n\n"))))))


(defn- write-extension [f filetype]
  (clojure.string/replace f #"\.cljx$" (str "." filetype)))

(defn- mv-files [cljx out]
  (map #(sh/sh "mv" % %2) cljx out))

(defn- package
  "Takes a filetype and sets all cljx files in the source-paths to it so they
  can be consumed nicely by downstream projects. Use this if you're writing a
  library with chenex."
  [filetype project]
  (if (or (nil? (some #(#{:clj :cljs} %) (map keyword filetype)))
        (not= 1 (count filetype)))
    (lmain/abort (red-text "Chenex Error: package requires either clj or cljs"))
    (let [src (:source-paths project)
          files (reduce #(into % (file-seq (File. %2))) [] src)
          cljx? (fn [f]
                  (re-find #"^(?:(?!#).)*\.cljx$" (str f)))
          cljx-files (->> files
                   (filter cljx?)
                   (map str))
          new-files (map #(write-extension % (first filetype)) cljx-files)]
      (doall (mv-files cljx-files new-files)))))


(defn chenex
  "Run the chenex compiler"
  {:help-arglists '([compile build repl package])
   :subtasks [#'compile #'build #'repl #'package]}
  ([project]
     (lmain/abort
      (lhelp/help-for "chenex")))
  ([project subtask & args]
     (case subtask
       "compile" (compile project)
       "build" (build args)
       "repl" (repl args)
       "package" (package args project)
       (lmain/abort (red-text (str "Chenex Error: Subtask " subtask " not found."))
                    (lhelp/subtask-help-for "chenex")))))
