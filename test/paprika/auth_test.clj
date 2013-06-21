(ns paprika.auth-test
  (:require [clj-http.client :as http-client]
            [clojure.test :refer :all]
            [paprika.auth :as auth]
            [paprika.http :as http]))

(deftest test-generate-server-auth-url
  (is (= "https://account.app.net/oauth/authenticate?response_type=code"
         (auth/generate-server-auth-url nil)))
  (is (= "https://account.app.net/oauth/authenticate?response_type=code"
         (auth/generate-server-auth-url {})))
  (is (= "https://account.app.net/oauth/authenticate?foo=bar&response_type=code"
         (auth/generate-server-auth-url (sorted-map :foo "bar")))))

(deftest test-generate-client-auth-url
  (is (= "https://account.app.net/oauth/authenticate?response_type=token"
         (auth/generate-client-auth-url nil)))
  (is (= "https://account.app.net/oauth/authenticate?response_type=token"
         (auth/generate-client-auth-url {})))
  (is (= "https://account.app.net/oauth/authenticate?foo=bar&response_type=token"
         (auth/generate-client-auth-url (sorted-map :foo "bar")))))

(deftest test-request-app-token
  (let [f (fn [method url data opts]
            (is (= :post method))
            (is (= auth/access-token-uri url))
            (is (= {:grant-type "client_credentials"} data))
            (is (= {:target-format :url-encoded} opts)))
        g (fn [method url data opts]
            (is (= :post method))
            (is (= auth/access-token-uri url))
            (is (= {:grant-type "client_credentials" :code "foo"} data))
            (is (= {:target-format :url-encoded} opts))
            (is (not (contains? data :force-permissions?))))
        h (fn [method url data opts]
            (is (= :post method))
            (is (= auth/access-token-uri url))
            (is (= {:grant-type "client_credentials"} data))
            (is (= {:target-format :url-encoded} opts)))]
    (binding [http/request f]
      (auth/request-app-token {}))
    (binding [http/request g]
      (auth/request-app-token {:code "foo"}))
    (binding [http/request h]
      (auth/request-app-token {:grant-type "foo"}))))

(deftest test-request-server-token
  (let [f (fn [method url data opts]
            (is (= :post method))
            (is (= auth/access-token-uri url))
            (is (= {:grant-type "authorization_code"} data))
            (is (= {:target-format :url-encoded} opts))
            (is (not (contains? data :force-permissions?))))
        g (fn [method url data opts]
            (is (= :post method))
            (is (= auth/authorize-uri url))
            (is (= {:grant-type "authorization_code" :code "foo"} data))
            (is (= {:target-format :url-encoded} opts))
            (is (not (contains? data :force-permissions?))))
        h (fn [method url data opts]
            (is (= :post method))
            (is (= auth/access-token-uri url))
            (is (= {:grant-type "authorization_code"} data))
            (is (= {:target-format :url-encoded} opts))
            (is (not (contains? data :force-permissions?))))]
    (binding [http/request f]
      (auth/request-server-token {}))
    (binding [http/request g]
      (auth/request-server-token {:code "foo" :force-permissions? true}))
    (binding [http/request h]
      (auth/request-server-token {:grant-type "foo"}))))
