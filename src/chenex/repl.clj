(ns chenex.repl
  (:require [cemerick.piggieback :as piggieback]
            cljs.closure
            [clojure.java.io :as io]
            [chenex.parser :as parse]
            [clojure.tools.nrepl.middleware :refer (set-descriptor!)]))


;; tried writing fresh but uff, ended up copying from cljx :p

(def chenex-load-rules (atom nil))

(defn- find-resource
  [name]
  (if-let [cl (clojure.lang.RT/baseLoader)]
    (.getResource cl name)
    (ClassLoader/getSystemResourceAsStream name)))

(defn- chenex-load
  "Loads Clojure code from resources in classpath. A path is interpreted as
  classpath-relative if it begins with a slash or relative to the root
  directory for the current namespace otherwise."
  {:added "1.0"}
  [& paths]
  (doseq [^String path paths]
    (let [^String path (if (.startsWith path "/")
                         path
                         (str (#'clojure.core/root-directory (ns-name *ns*)) \/ path))]
      (when @#'clojure.core/*loading-verbosely*
        (printf "(clojure.core/load \"%s\")\n" path)
        (flush))
      (#'clojure.core/check-cyclic-dependency path)
      (when-not (= path (first @#'clojure.core/*pending-paths*))
        (with-bindings {#'clojure.core/*pending-paths* (conj @#'clojure.core/*pending-paths* path)}
          (let [base-resource-path (.substring path 1)
                cljx-path (str base-resource-path ".cljx")]
            (if-let [cljx (find-resource cljx-path)]
              (do
                (when @#'clojure.core/*loading-verbosely*
                  (printf "Transforming cljx => clj from %s.cljx\n" base-resource-path))
                (-> (slurp cljx)
                    (parse/repl-parse @chenex-load-rules)
                    java.io.StringReader.
                    (clojure.lang.Compiler/load base-resource-path
                                                (last (re-find #"([^/]+$)" cljx-path)))))
              (clojure.lang.RT/load base-resource-path))))))))


(def ^:private clojure-load load)
(def ^:private clojure-resource io/resource)

(defn chenex-cljs-resource
  "Converts cljx files into cljs and passes them to install-chenex-load
  for loading"
  [& [^String resource-name :as resource-args]]
  (or (apply clojure-resource resource-args)
      (when-let [cljx (and (.endsWith resource-name ".cljs")
                           (apply clojure-resource (cons (.replaceAll resource-name ".cljs$" ".cljx")
                                                         (rest resource-args))))]
        (let [tmp-cljs (java.io.File/createTempFile "cljxtransform" ".cljs")]
          (.deleteOnExit tmp-cljs)
          (as-> (slurp cljx) %
                (parse/repl-parse % @chenex-load-rules)
                (spit tmp-cljs %))
          (.toURL tmp-cljs)))))

(def ^:private install-chenex-load
  (delay (alter-var-root #'load (constantly chenex-load))
         (alter-var-root #'cljs.closure/cljs-dependencies
                         (fn [cljs-dependencies]
                           (fn [& args]
                             (with-redefs [io/resource chenex-cljs-resource]
                               (apply cljs-dependencies args)))))))

;; this is pretty awful, so many other things to work on though...
;; FIXME: not guaranteed to find build file but this is only for my use (fix later)
(defn wrap-chenex
  "Middleware for loading and evaluating chenex code"
  ([h] (wrap-chenex h (-> "builds/chenex-repl.clj" slurp read-string)))
  ([h rules]
     (reset! chenex-load-rules rules)
     @install-chenex-load
     (fn [{:keys [op code file file-name session] :as msg}]
       (cond
        (and (= op "eval") code)
        (h (assoc msg :code (if-let [check (parse/repl-parse code rules)]
                              check "")))  ;makes sure code is not nil

        (and (= op "load-file") file (re-matches #".+\.cljx$" file-name))
        (h (assoc msg :file (if-let [check (parse/repl-parse file rules)]
                              check "")))

        :else (h msg)))))

(set-descriptor! #'wrap-chenex
  {:requires #{"clone"}
   :expects #{#'piggieback/wrap-cljs-repl}
   :handles {}})
