(ns paprika.more
  "A place for functions built on top of paprika.core functions."
  (:require [paprika.core :as core]
            [paprika.util :as util]))

(defn me
  "Retrieve the currently authenticated user."
  [args]
  (core/lookup-user (assoc args :id "me")))

(defn fill
  "Perform a sequence of requests using a given function from core,
  passing a value from the response metadata to the next request's query params,
  concatenate the results and return when a certain predicate returns true.

  Options:

    :meta-key

      Type: IFn (usually a Keyword)

      Specifies the function used to extract the value to pass to the next request
      from the response's metadata. Eg. :max-id.

    :params-key

      Type: Keyword

      Specifies the key that will be associated to the param returned by :meta-key
      in the next request's params.

    :reverse-concat

      Type: Boolean

      Specifies whether to concatenate results of the requests in reverse order.

    :stop-pred

      Type: IFn [run meta data]

      The predicate that decides whether to break recursion, stopping the requests
      and returning the current results.

    :f

      Type: IFn [params]

      The request function (from paprika.core).
      If it requires any arguments before params, use clojure.core/partial.

    :params

      Type: Map

      The base params map that will be passed to the request function (:f)."
  [& {:keys [meta-key params-key reverse-concat stop-pred f params]
      :or {meta-key :min-id
           params-key :before-id
           reverse-concat false}}]
  (loop [run 0
         value (params-key params)
         results '()]
    (let [run (inc run)
          params (-> params
                     (assoc :return :envelope)
                     (assoc params-key value))
          {:keys [data meta]} (f params)
          results (if reverse-concat
                    (concat data results)
                    (concat results data))]
      (if (stop-pred run data meta)
        results
        (recur run
               (meta-key meta)
               results)))))

(defn backfill
  "Retrieve all objects using a given function from a given id (inclusive) to now.
  If no function is given, paprika.core/retrieve-unified-stream is used."
  ([since-id params] (backfill core/retrieve-unified-stream since-id params))
  ([f since-id params]
   (let [since-id (util/as-long since-id)]
     (->> (fill :f f
                :params params
                :stop-pred (fn [run data meta]
                             (->> (map (comp util/as-long :id) data)
                                  (not-any? #(< % since-id))
                                  not)))
          (filter #(>= (util/as-long (:id %)) since-id))))))

(defn unpaginated
  "Retrieve objects using a given function, unpaginated.
  Eg. (unpaginated paprika.core/retrieve-unified-stream {:count \"300\" :token token})
  will return 300 last posts in the unified stream using two 200-post requests."
  [f params]
  (let [full-count (util/as-long (:count params))
        needed-runs (long (Math/ceil (/ full-count 200)))]
    (->> (fill :f f
               :params (assoc params :count "200")
               :stop-pred (fn [run data meta]
                            (= run needed-runs)))
         (take full-count))))
