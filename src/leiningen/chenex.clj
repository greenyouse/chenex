(ns leiningen.chenex
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.pprint :refer [pprint]]
            [leiningen.core.eval :refer (eval-in-project)]
            [leiningen.core.main :as lmain]
            [leiningen.help :as lhelp]
            [plugin-helpers.core :as h])
  (:refer-clojure :exclude [compile])
  (import java.io.File))

(defn- compile
  "Compiles cljx files into new formats"
  [project]
  (let [log# (if (false? (h/get-project-value :chenex :log))
               false true)]
    (when log#
      (h/info "Compiling files for chenex\n"))
    (if-let [builds# (h/get-project-value :chenex :builds)]
      (eval-in-project project
        `(do (#'greenyouse.chenex.core/run '~builds#)
             ~(when (-> project :eval-in name (= "subprocess"))
                '(shutdown-agents)))
        '(require 'greenyouse.chenex.core))
      (when log#
        (h/warning "Chenex Error: No builds detected in project.clj")))))


;;happy to add more templates here
(defn- build
  "Writes a basic build template, options are: cljx"
  [template]
  (h/info "Writing a new chenex configuration.\n")
  (let [t# (first template)
        loc# (try (-> (str "greenyouse/chenex/templates/" t# ".clj")
                    io/resource
                    slurp)
                  (catch Exception _
                    (lmain/abort (h/red-text
                                   (format "Chenex Error: template %s not found.\n\n Options are: cljx" (first template))))))]
    (h/assoc-in-project [:chenex :builds] (vec t#))))


(defn- repl
  "Sets a new target environment for the REPL iteractively. Enter the name
  of a chenex feature environement"
  [env]
  (h/info "Changing the chenex REPL.\n")
  (let [e# (reduce #(conj % (keyword %2)) #{} env)]
    (try (h/assoc-in-project [:chenex :repl] e#)
         (catch Exception _ (lmain/abort (h/red-text "Chenex Error: enter the name of a chenex feature\n\n"))))))


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
    (lmain/abort (h/red-text "Chenex Error: package requires either clj or cljs"))
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
       (lmain/abort (h/red-text (str "Chenex Error: Subtask " subtask " not found."))
                    (lhelp/subtask-help-for "chenex")))))
