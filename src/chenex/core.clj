(ns chenex.core
  (:require [chenex.parser :as p]
            [clojure.string :as st]
            [clojure.java.io :as io])
  (import java.io.File))

(defn- get-source-files
  "Finds any cljx source files in a directory.
  Returns a vector of the files' URIs."
  [dir]
  (->> (file-seq (File. dir))
       (filter #(re-find #"^(?:(?!#).)*\.cljx$" (str %)))
       (mapv str)))

;; taken from cljx
(defn- relativize [source-path f]
  (letfn [(to-uri [i]
            (-> i io/file .toURI))]
    (.relativize  (to-uri source-path) (to-uri f))))

(defn- write-extension [f filetype]
  (clojure.string/replace f #"\.cljx$" (str "." filetype)))

(defn- parse-src
  "Takes a single source directory from source-paths (with a few build options)
  and passes the data to the parser for compilation."
  [src output-path filetype features inner-transforms outer-transforms]
  (let [in-files (get-source-files src)
        out (if-not (= \/ (last output-path))
              (str output-path "/") output-path)
        out-files (mapv #(as-> (relativize src %) f
                               (str out f)
                               (write-extension f filetype)) in-files)]
    (doall (pmap #(p/start-parse % %2 features inner-transforms outer-transforms) in-files out-files))))

(defn- read-build [{:keys [source-paths output-path rules]}]
  (let [{:keys [filetype features inner-transforms outer-transforms]}  rules]
    (doall (pmap #(parse-src % output-path filetype features inner-transforms outer-transforms) source-paths))))

(defn run [builds]
  (doall (pmap read-build builds)))
