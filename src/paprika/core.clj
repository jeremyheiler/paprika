(ns paprika.core
  "This namespace contains a function for (almost) every App.net endpoint.

  Each endpoint function has an opts parameter for various options
  that affect the request or response. Unless noted, any values in
  this map will become part of the query string.

  Global Options:

    :token

      This is the access or delegate token used to authenticate the
      request. This value is put on the request as the Authorization
      header. Some endpoints work without an access token.

  The opts parameter for any endpoint function is an optional map that contains
  Authenticated requests require an :token key in the opts map.

  Whenever a \"user-id\" is required, one of the following can be used:
    - The user's ID (as an integer or, preferably, as a string).
    - The user's username with the @ symbol prefixed, i.e. \"@literally\".
    - The string \"me\" for the currently authenticated user.

  General Parameters:

    :include-annotations

      Type: integer; 0 or 1

      Specifies whether or not annotations should be included in the
      response.

    :include-user-annotations

      Type: integer; 0 or 1

      Specifies whether or not user annotations should be included in
      the response.

    :include-html

      Type: integer; 0 or 1

      Specifies whether or not the description's :html field should be
      included along with the :text field.
  "
  (:require [clojure.string :as string]
            [cheshire.core :as json]
            [pantomime.mime :as mime]
            [paprika.util :as util]
            [paprika.http :as http]))

(defn- join-ids [user-ids]
  (string/join "," user-ids))

(defn lookup-token
  "Lookup the currently used OAuth access token."
  [& [opts]]
  (http/api-request :get "/token" opts))

(defn deauthorize-token
  "Deauthorize the currently used OAuth access token."
  [& [opts]]
  (http/api-request :delete "/token" opts))

(defn lookup-authorized-user-ids
  "Lookup IDs of users that have authorized the current app.
  Must be requested using an app access token."
  [& [opts]]
  (http/api-request :get "/app/me/tokens/user_ids" opts))

(defn lookup-authorized-user-tokens
  "Lookup authorized user tokens for the current app.
  Must be requested using an app access token."
  [& [opts]]
  (http/api-request :get "/app/me/tokens" opts))

(defn lookup-user
  "Lookup a specific user."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id) opts))

(defn lookup-users
  "Lookup multiple users by their \"user-id\". The ids must be
  provided as a sequential collection like a vector."
  [user-ids & [opts]]
  (http/api-request :get "/users" (assoc opts :ids (join-ids user-ids))))

(defn update-user
  "Update the specified fields for the currently authenticated user."
  [data & [opts]]
  (http/api-request :patch "/users/me" data opts))

(defn update-user-object ;; TODO This needs a better name.
  "Update the current user's profile."
  [data & [opts]]
  (http/api-request :put "/users/me" data opts))

(defn lookup-avatar
  "Lookup the bytes for the user's avatar."
  [user-id & [opts]]
  (http/api-request :get
                    (str "/users/" user-id "/avatar")
                    (assoc opts :return-format :byte-array)))

;; TODO upload-avatar

(defn lookup-cover
  "Lookup the bytes for the user's cover."
  [user-id & [opts]]
  (http/api-request :get
                    (str "/users/" user-id "/cover")
                    (assoc opts :return-format :byte-array)))

;; TODO upload-cover

(defn follow-user
  "Follow the specified user."
  [user-id & [opts]]
  (http/api-request :post (str "/users/" user-id "/follow") opts))

(defn unfollow-user
  "Unfollow the specified user."
  [user-id & [opts]]
  (http/api-request :delete (str "/users/" user-id "/follow") opts))

(defn mute-user
  "Mute the specified user."
  [user-id & [opts]]
  (http/api-request :post (str "/users/" user-id "/mute") opts))

(defn unmute-user
  "Unmute the specified user."
  [user-id & [opts]]
  (http/api-request :delete (str "/users/" user-id "/mute") opts))

(defn block-user
  "Block the specified user."
  [user-id & [opts]]
  (http/api-request :post (str "/users/" user-id "/block" opts)))

(defn unblock-user
  "Unblock the specified user."
  [user-id & [opts]]
  (http/api-request :delete (str "/users/" user-id "/block") opts))

(defn search-users
  "Search for users. Optional :count"
  [search-query & [opts]]
  (http/api-request :get "/users/search" (assoc opts :q search-query)))

(defn lookup-following
  "Lookup the users the specified user is following."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/following") opts))

(defn lookup-following-ids
  "Lookup the IDs of the users the specified user is following."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/following/ids") opts))

(defn lookup-followers
  "Lookup the users following the specified user."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/followers") opts))

(defn lookup-followers-ids
  "Lookup the IDs of the users following the specified user."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/followers/ids") opts))

(defn lookup-muted-users
  "Lookup the users muted by the specified user."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/muted") opts))

(defn lookup-muted-user-ids
  "Lookup the IDs of the users muted by the specified users."
  [user-ids & [opts]]
  (http/api-request :get "/users/muted/ids" (assoc opts :ids (join-ids user-ids))))

(defn lookup-blocked-users
  "Lookup the users blocked by the specified user."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/blocked") opts))

(defn lookup-blocked-user-ids
  "Lookup the IDs of the users blocked by the specified users."
  [& [opts]]
  (http/api-request :get "/users/blocked/ids" opts))

(defn lookup-repost-users
  "Lookup the users who have reposted the specified post."
  [post-id & [opts]]
  (http/api-request :get (str "/posts/" post-id "/reposters") opts))

(defn lookup-star-users
  "Lookup the users who have starred the specified post."
  [post-id & [opts]]
  (http/api-request :get (str "/posts/" post-id "/stars") opts))

(defn- coerce-post-data [data]
  (if (string? data) {:text data} data))

(defn create-post
  "Create a new post. The first parameter could be a string or a map.
  If it's a string, it will be sent as the text of the post. If it's a
  map, then it requires the :text field. If the post is a reply, then
  it must also contain the :reply-to field, which is the post ID of
  the parent post. Post annotations can also be provided."
  [data & [opts]]
  (http/api-request :post "/posts" (coerce-post-data data) opts))

(defn lookup-post
  "Lookup the specified post."
  [post-id & [opts]]
  (http/api-request :get (str "/posts/" post-id) opts))

(defn lookup-posts
  "Lookup multiple specified posts."
  [post-ids & [opts]]
  (http/api-request :get "/posts" (assoc opts :ids (join-ids post-ids))))

(defn lookup-user-posts
  "Lookup posts from a specified user."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/posts") opts))

(defn find-posts
  "Search for posts matching a query."
  [opts]
  (http/api-request :get "/posts/search" opts))

(defn delete-post
  "Delete the specified post."
  [post-id & [opts]]
  (http/api-request :delete (str "/posts/" post-id) opts))

(defn repost-post ;; TODO share-post?
  "Share a post with the current user's followers (repost)."
  [post-id & [opts]]
  (http/api-request :post (str "/posts/" post-id "/repost") opts))

(defn unrepost-post ;; TODO unshare-post?
  "Undo sharing a post with the current user's followers (unrepost)."
  [post-id & [opts]]
  (http/api-request :delete (str "/posts/" post-id "/repost") opts))

(defn star-post
  "Star the specified post."
  [post-id & [opts]]
  (http/api-request :post (str "/posts/" post-id "/star") opts))

(defn unstar-post
  "Star the specified post."
  [post-id & [opts]]
  (http/api-request :delete (str "/posts/" post-id "/star") opts))

(defn retrieve-posts-created-by-user
  "Get the most recent posts created by a specific user in reverse
  post order."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/posts") opts))

(defn retrieve-posts-starred-by-user
  "Get the most recent posts starred by a specific user in reverse
  post order"
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/stars") opts))

(defn retrieve-posts-with-mention
  "Get the most recent posts mentioned by a specific user in reverse
  post order."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/mentions") opts))

(defn retrieve-tagged-posts
  "Lookup the most recent posts for a specific hashtag."
  [hashtag & [opts]]
  (http/api-request :get (str "/posts/tag/" hashtag) opts))

(defn retrieve-replies
  "Lookup the replies to a post."
  [post-id & [opts]]
  (http/api-request :get (str "/posts/" post-id "/replies") opts))

(defn retrieve-stream
  "Lookup the most recent posts from the current user and the users they follow."
  [& [opts]]
  (http/api-request :get "/posts/stream" opts))

(defn retrieve-unified-stream
  "Lookup the most recent posts from the current user's
  personalized stream and mentions stream merged into one stream."
  [& [opts]]
  (http/api-request :get "/posts/stream/unified" opts))

(defn retrieve-global-stream
  "Lookup the most recent posts from the global stream."
  [& [opts]]
  (http/api-request :get "/posts/stream/global" opts))

(defn report-post
  "Report the specified post."
  [post-id & [opts]]
  (http/api-request :post (str "/posts/" post-id "/report") opts))

(defn lookup-place
  "Lookup a specified place."
  [factual-id & [opts]]
  (http/api-request :get (str "/places/" factual-id) opts))

(defn search-places
  "Search for places matching specified geographical location."
  [latitude longitude & [opts]]
  (http/api-request :get "/places/search"
                    (assoc opts
                           :latitude latitude
                           :longitude longitude)))

(defn lookup-interactions
  "Lookup interactions for the current user."
  [& [opts]]
  (http/api-request :get "/users/me/interactions" opts))

(defn process-text
  "Test how App.net will parse text for entities (posts, messages, user profiles)
  as well as render text as html. The first parameter could be a string or a map.
  If it's a string, it will be used as the text.
  If it's a map, then it requires the :text field."
  [data & [opts]]
  (http/api-request :post "/text/process" (coerce-post-data data) opts))

(defn config-vars
  "Get variables which define the current behavior of the App.net platform."
  [& [opts]]
  (http/api-request :get "/config" opts))

(defn mark-stream
  "Update a Stream Marker (the current user's place in a stream)."
  [data & [opts]]
  (http/api-request :post "/posts/marker" data opts))

(defn lookup-files
  "Lookup the current user's files."
  [& [opts]]
  (http/api-request :get "/users/me/files" opts))

(defn lookup-file-contents
  "Get the contents of a specific file."
  [file-id & [opts]]
  (http/api-request :get (str "/files/" file-id "/content") opts))

(defn- utf8-bytes [s] (.getBytes s "UTF-8"))

(defn- do-upload-file [data opts]
  (http/api-request :post "/files"
    (-> opts
        (dissoc :kind :type :name :public :annotations)
        (assoc :http-options {:multipart [
          (assoc data :part-name "content")
          {:name "metadata.json"
           :part-name "metadata"
           :content (-> (select-keys opts [:kind :type :name :public :annotations])
                        (merge (:additional-metadata opts)) ; being future-proof
                        (json/encode {:keyfn util/encode-key})
                        utf8-bytes)
           :mime-type "application/json"}
          ]}))))

(defn upload-file
  "Upload a new file.
  You can use a File, an InputStream, a byte array or a string.
  If you use a File, you can omit the filename.

  Options:

    :type

      Type: string

      Specifies an App.net file type (eg. \"com.example.my-cool-app.hipster-selfie\".)
      Required.

    :mime-type

      Type: string

      Specifies a MIME type (eg. \"application/pdf\".)
      Optional, but recommended. If you omit it, it will be guessed.

    :additional-metadata

      Type: map

      Specifies any metadata you want to pass to the API.
      You can also use the keys :kind, :annotations and :public directly.
      Optional."
  ([file opts]
   {:pre [(instance? java.io.File file)]}
   (upload-file (.getName file) file opts))
  ([filename content opts]
   (let [content (if (string? content) (utf8-bytes content) content)]
     (do-upload-file
       {:name filename
        :mime-type (or (:mime-type opts) (mime/mime-type-of content))
        :content content}
       opts))))
