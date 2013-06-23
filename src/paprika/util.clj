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

(defn encode-bool
  [val]
  (cond
   (true? val) 1
   (false? val) 0
   :else val))

(defn transform
  "Recursively transforms all map keys with key transform function,
  and map vals with the value transform function."
  [key-t val-t coll]
  (letfn [(f [t form]
            (if (map? form)
              (into {} (map t form))
              form))
          (g [[k v]]
            [(if key-t (key-t k) k)
             (if val-t (val-t v) v)])]
    (walk/postwalk (partial f g) coll)))

(defn encode
  [coll]
  (transform encode-key encode-bool coll))

(defn decode
  [coll]
  (transform decode-key nil coll))
