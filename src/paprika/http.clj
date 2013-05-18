(ns paprika.http
  (:require [clojure.string :as string]
            [clojure.walk :as walk]
            [clj-http.client :as http-client]
            [cheshire.core :as json]))

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

(defn ^:private with-json-content
  [client]
  (fn [request]
    (clojure.pprint/pprint request)
    (let [req (update-in request [:form-params] (partial transform-keys encode-key))
          _ (clojure.pprint/pprint req)
          response (client req)]
      (if-let [body (:body response)]
        (assoc response :body (json/decode body decode-key))
        response))))

(defn request
  ([method url req]
     (let [req (update-in req [:form-params] (partial transform-keys encode-key))
           res (http-client/request (assoc req
                                      :throw-exceptions false
                                      :method method
                                      :url url))]
       (if-let [body (:body res)]
         (assoc res :body (json/decode body decode-key))
         res)))
  ([method url req token]
     (request method url (assoc req :oauth-token token))))
