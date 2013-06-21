(ns paprika.more
  "A place for functions built on top of paprika.core functions."
  (:require [paprika.core :as core]))

(defn me
  "Retrieve the currently authenticated user."
  [args]
  (core/lookup-user (assoc args :id "me")))
