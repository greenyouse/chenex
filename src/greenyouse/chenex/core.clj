(ns greenyouse.chenex.core
  (:require [greenyouse.chenex.parser :as p]
            [clojure.string :as st]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]])
  (import java.io.File))

(defn- get-source-files
  "Finds any cljx source files in a directory.
  Returns a vector of the files' URIs."
  [dir]
  (let [matches (file-seq (File. dir))
        files (filter #(.isFile %) matches)
        cljx? (fn [f]
                (re-find #"^(?:(?!#).)*\.cljx$" (str f)))]
                (reduce (fn [m f]
                          (if (cljx? f)
                            (update-in m [:to-parse] #(conj % (str f)))
                            (update-in m [:dont-parse] #(conj % (str f)))))
                  {:to-parse []
                   :dont-parse []} files)))

;; taken from cljx
(defn- relativize [source-path f]
  (letfn [(to-uri [i]
            (-> i io/file .toURI))]
    (.relativize  (to-uri source-path) (to-uri f))))

(defn- write-extension [f filetype]
  (clojure.string/replace f #"\.cljx$" (str "." filetype)))

(defn- parse-src
  "Takes a single source directory from source-paths (with a few build options)
  and passes any .cljx files to the parser."
  [src output-path filetype features inner-transforms]
  (let [{:keys [to-parse dont-parse]} (get-source-files src)
        out (if-not (= \/ (last output-path))
              (str output-path "/") output-path)
        to-parse-out (mapv #(as-> (relativize src %) f
                              (str out f)
                              (write-extension f filetype)) to-parse)
        dont-parse-out (mapv #(as-> (relativize src %) f
                                (str out f)) dont-parse)]
    (doall (pmap #(p/start-parse % %2 features inner-transforms)
             to-parse to-parse-out))
    (doall (pmap #(do (io/make-parents %2)
                      (spit %2 (slurp %))) dont-parse dont-parse-out))))

(defn- read-build [{:keys [source-paths output-path rules]}]
  (let [{:keys [filetype features inner-transforms]}  rules]
    (doall (pmap #(parse-src % output-path filetype features
                    inner-transforms) source-paths))))

(defn run [builds]
  (doall (pmap read-build builds)))
