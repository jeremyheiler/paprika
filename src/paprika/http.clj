(ns paprika.http
  (:require [clj-http.client :as http-client]
            [cheshire.core :as json]
            [paprika.util :as util]))

(defn wrap-query-params
  "Dash-case the query parameters if they're present."
  [client]
  (fn [request]
    (client (if-let [params (:query-params request)]
              (assoc request :query-params (util/encode params))
              request))))

(defn wrap-form-params
  "Dash-case the form parameters if they're present."
  [client]
  (fn [request]
    (client (let [body (:body request)]
              (if (and body (= :url-encoded (:target-format request)))
                (assoc (dissoc request :body) :form-params (util/encode body))
                request)))))

(defn wrap-json
  "Ensure the HTTP body of a request is converting to JSON and the
  HTTP body of the response is converted to EDN."
  [client]
  (fn [request]
    (let [request (if-let [body (:body request)]
                    (assoc request
                      :body (json/encode body {:key-fn util/encode-key})
                      :content-type "application/json")
                    request)
          response (client request)
          content-type (get-in response [:headers "content-type"])
          return-format (:return-format request)
          return (:return request)]
      (if (and (= content-type "application/json") (= :clojure return-format))
        (update-in response [:body] json/decode util/decode-key)
        response))))

(defn ^:dynamic raw-request
  [request]
  (let [m [#'wrap-json #'wrap-query-params #'wrap-form-params]]
    (http-client/with-middleware (concat http-client/default-middleware m)
      (http-client/request request))))

(defn ^:dynamic request
  "Make an HTTP request to the API.

  Non-App.net Options:

    :target-format - The format the data needs to be on the request.

      :json (default)
      :url-encoded

    :return-format - The format of the response envelope.

      :clojure (default)
      :json
      :byte-array

    :debug? - If true, attache the HTTP response to the envelope as metadata
  "
  [method url data opts]
  (let [request {:oauth-token (or (:token opts) (:access-token opts))
                 :target-format (:target-format opts :json)
;;                 :accept-format (:accept-format opts :clojure)
                 :return-format (:return-format opts :clojure)
                 :throw-exceptions false
                 :method method
                 :url url}
        request (merge request (:http-options opts))
        request (cond-> request
                        (= (:return-format opts) :byte-array)
                        (assoc :as :byte-array))
        debug? (:debug? opts)
        opts (dissoc opts
                     :access-token
                     :token
                     :target-format
                     :return-format
                     :debug?
                     :http-options)
        request (-> request
                    (cond-> (seq opts) (assoc :query-params opts))
                    (cond-> (seq data) (assoc :body data)))
        response (raw-request request)
        envelope (:body response)]
    (if (and (= :clojure (:return-format request)) (<= 400 (:status response)))
      (let [ex #(ex-info % envelope)]
        (cond
         (:meta envelope) (throw (ex (get-in envelope [:meta :error-message])))
         (:error envelope) (throw (ex (:error envelope)))
         :else (throw (ex "Unknown error"))))
      (if debug?
        (with-meta envelope {:response response})
        envelope))))

(defn api-request
  ([method path opts]
     (api-request method path {} opts))
  ([method path data opts]
     (let [url (str "https://alpha-api.app.net/stream/0" path)]
       (request method url data opts))))
