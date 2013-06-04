(ns paprika.auth
  (:require [paprika.http :as http]
            [clj-http.client :as http-client]))

(def authenticate-uri "https://account.app.net/oauth/authenticate")
(def access-token-uri "https://account.app.net/oauth/access_token")
(def authorize-uri "https://account.app.net/oauth/authorize")

(defn request-app-token
  "Sends a request for an access token that is tied to your
  application and not a specific user.

  Required args
   :client-id
   :client-secret"
  [args]
  (let [params (assoc args :grant-type "client_credentials")
        req {:form-params (http/transform-keys http/encode-key params)}]
    (:body (http/request :post access-token-uri req))))

(defn request-server-token
  "Sends a request for an access token that is tied to a user.

  Required args
   :client-id
   :client-secret
   :redirect-uri
   :code"
  [args]
  (let [params (assoc args :grant-type "authorization_code")
        req {:form-params (http/transform-keys http/encode-key params)}]
    (:body (http/request :post access-token-uri req))))

(defn ^:private generate-auth-url
  [args]
  (let [args (http/transform-keys http/encode-key args)]
    (str authenticate-uri "?" (http-client/generate-query-string args))))

(defn generate-server-auth-url
  "Returns the correct URL for authenticating a server.

  Required args:
   :client-id
   :redirect-uri
   :scope

  Optional args:
   :state"
  [args]
  (generate-auth-url (assoc args :response-type "code")))

(defn generate-client-auth-url
  "Returns the correct URL for authenticating a client.

  Required args:
   :client-id
   :redirect-uri
   :scope

  Optional args:
   :state"
  [args]
  (generate-auth-url (assoc args :response-type "token")))
