(ns paprika.util-test
  (:require [clj-http.client :as http-client]
            [clojure.test :refer :all]
            [paprika.util :refer :all]))

(deftest test-decode-key
  (is (= :foo (decode-key "foo")))
  (is (= :foo-bar (decode-key "foo_bar"))))

(deftest test-encode-key
  (is (= "foo" (encode-key :foo)))
  (is (= "foo_bar" (encode-key :foo-bar))))

(deftest test-inverse-encoding
  (is (= :foo (decode-key (encode-key :foo))))
  (is (= :foo-bar (decode-key (encode-key :foo-bar))))
  (is (= "foo" (encode-key (decode-key "foo"))))
  (is (= "foo_bar" (encode-key (decode-key "foo_bar")))))

(deftest test-encode-bool
  (is (= 1 (encode-bool true)))
  (is (= 0 (encode-bool false)))
  (is (= :foo (encode-bool :foo))))
