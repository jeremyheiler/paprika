(ns paprika.http
  (:require [clojure.string :as string]
            [clojure.walk :as walk]
            [clj-http.client :as http-client]
            [cheshire.core :as json]))

(def host "https://alpha-api.app.net")

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

(defn request
  ([method url req]
     (let [req (assoc req
                 :throw-exceptions false
                 :method method
                 :url url)
           _ (clojure.pprint/pprint req)
           res (http-client/request req)
           ]
       (if-let [body (:body res)]
         (assoc res :body (json/decode body decode-key))
         res)))
  ([method url req token]
     (request method url (assoc req :oauth-token token))))

(defn split-path
  "Split a path into a sequence of path segments."
  [path]
  (letfn [(f [segment]
            (if (.startsWith segment ":")
              (keyword (subs segment 1))
              segment))]
    (map f (string/split path #"/"))))

(defn replace-keys
  "Replaces the keywords in the split path with their values."
  [path-segments input]
  (letfn [(f [segment]
            (if (keyword? segment)
              (str (get input segment))
              segment))]
    (string/join "/" (map f path-segments))))

(defn apply-handlers
  ""
  [input handlers]
  (letfn [(f [new-input [k v]]
            (update-in new-input [k] v))]
    (reduce f input handlers)))

(defmacro define-endpoint
  ""
  [endpoint-name http-method path & opts]
  (let [opts (apply hash-map opts)
        path-segments (split-path path)
        replace-keys? (seq (filter keyword? path-segments))
        input (gensym)]
    `(defn ~endpoint-name
       ([input#]
          (~endpoint-name input# nil))
       ([~input token#]
          (let [~input ~(if-let [handlers (:handlers opts)]
                          (list apply-handlers input handlers)
                          input)]
            (request ~http-method
                     ~(if replace-keys?
                        (list str (str host "/stream/0") (list replace-keys (vec path-segments) input))
                        (str host "/stream/0" path))
                     (merge
                      (when-let [body# (select-keys ~input ~(:body-keys opts))]
                        {:content-type "application/json"
                         :body (json/encode body# {:key-fn encode-key})})
                      (when-let [params# (select-keys ~input ~(:query-keys opts))]
                        {:query-params params#}))
                     token#))))))
