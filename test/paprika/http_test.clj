(ns paprika.http-test
  (:require [clj-http.client :as http-client]
            [clojure.test :refer :all]
            [paprika.http :refer :all]))

(deftest test-wrap-query-params
  (let [f (fn [request]
            (is (not (contains? request :query-params))))
        g (fn [request]
            (is (contains? request :query-params))
            (is (= {"foo" "bar" "foo_bar" "baz"} (:query-params request))))]
    (let [request {}]
      ((wrap-query-params f) request))
    (let [request {:query-params (sorted-map :foo "bar" :foo-bar "baz")}]
      ((wrap-query-params g) request))))

(deftest test-wrap-forms-params
  (let [f (fn [request]
            (is (empty? request)))
        g (fn [request]
            (is (= {:body {}} request)))
        h (fn [request]
            (is (= {:body {} :target-format :foo} request)))
        i (fn [request]
            (is (contains? request :form-params))
            (is (not (contains? request :body)))
            (is (= {"foo_bar" "baz"} (:form-params request))))]
    (let [request {}]
      ((wrap-form-params f) request))
    (let [request {:body {}}]
      ((wrap-form-params g) request))
    (let [request {:body {} :target-format :foo}]
      ((wrap-form-params h) request))
    (let [request {:body {:foo-bar "baz"} :target-format :url-encoded}]
      ((wrap-form-params i) request))))

(deftest test-wrap-json
  (let [f (fn [request]
            (is (empty? request)))
        g (fn [request]
            (is (= "application/json" (:content-type request)))
            (is (= "{\"foo_bar\":\"baz\"}" (:body request))))]
    (let [request {}]
      ((wrap-json f) request))
    (let [request {:body {:foo-bar "baz"}}]
      ((wrap-json g) request))
    (let [request {}
          ;;response ((wrap-json h) request)
          ])))
