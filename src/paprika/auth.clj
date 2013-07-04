(ns paprika.auth
  (:require [paprika.http :as http]
            [paprika.util :as util]
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
  [data]
  (let [data (assoc data :grant-type "client_credentials")]
    (http/request :post access-token-uri data {:target-format :url-encoded
                                               :return :envelope})))

(defn request-server-token
  "Sends a request for an access token that is tied to a user.

  Required args
   :client-id
   :client-secret
   :redirect-uri
   :code

  Optional args:
   :force-permissions?"
  [data]
  (let [url (if (:force-permissions? data) authorize-uri access-token-uri)
        data (-> data
                 (dissoc :force-permissions?)
                 (assoc :grant-type "authorization_code"))]
    (http/request :post url data {:target-format :url-encoded
                                  :return :envelope})))

(defn ^:private generate-auth-url
  [args]
  (let [args (util/encode args)]
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
