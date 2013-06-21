(ns paprika.util
  (:require [clojure.string :as string]
            [clojure.walk :as walk]))

(defn decode-key
  "Convert key from \"json_case\" to :clojure-case."
  [key]
  (keyword (string/replace key #"_" "-")))

(defn encode-key
  "Convert key from :clojure-case to \"json_case\"."
  [key]
  (string/replace (name key) #"-" "_"))

(defn transform-keys
  "Recursively transforms all map keys in coll with t."
  [t coll]
  (let [f (fn [[k v]] [(t k) v])]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) coll)))

(defn encode
  [coll]
  (transform-keys encode-key coll))

(defn decode
  [coll]
  (transform-keys decode-key coll))
