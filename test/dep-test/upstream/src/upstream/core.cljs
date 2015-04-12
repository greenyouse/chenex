(ns upstream.core
  (:require [greenyouse.chenex :as chenex]))

(enable-console-print!)

(defn example [msg]
  (chenex/in-case!
    [:opera] (println (str "opera: " msg))
    [:chrome] (println (str "chrome: " msg))
    :else
    (println (str "something else: " msg))))
