(ns paprika.core
  "This namespace contains a function for every App.net endpoint.

  Each endpoint function has an opts parameter for various options
  that effect the request or response. Unless noted, any values in
  this map will become part of the query string.

  Global Options:

    :access-token

      This is the access token used to authenticate the request. This
      value is put on the request as the Authorization header. Some
      endpoints work without an access token.

  The opts parameter for any endpoint function is an optional map that contains
  Authenticated requests require an :access-token key in the opts map.

  Whenever a \"user-id\" is required, one of the following can be used:
    - The user's ID (as an integer or, preferably, as a string).
    - The user's username with the @ symbol prefixed, i.e. \"@literally\".
    - The string \"me\" for the currently authenticated user.

  General User Parameters:

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
            [paprika.http :as http]))

(defn- join-ids
  [user-ids]
  (string/join "," user-ids))

(defn lookup-token
  [& [opts]]
  (http/api-request :get "/token" opts))

(defn deauthorize-token
  [& [opts]]
  (http/api-request :delete "/token" opts))

(defn lookup-authorized-user-ids
  [& [opts]]
  (http/api-request :get "/app/me/tokens/user_ids" opts))

(defn lookup-authorized-user-tokens
  [& [opts]]
  (http/api-request :get "/app/me/tokens" opts))

(defn lookup-user
  "Returns the specified user."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id) opts))

(defn lookup-users
  "Returns the users specified by their \"user-id\". The ids must be
  provided as a sequential collection like a vector."
  [user-ids & [opts]]
  (http/api-request :get "/users" (assoc opts :ids (join-ids user-ids))))

(defn update-user
  "Update the given fields for the currently authenticated user."
  [data & [opts]]
  (http/api-request :patch "/users/me" data opts))

(defn update-user-object ;; TODO This needs a better name.
  "Update the currently authenticated user object."
  [data & [opts]]
  (http/api-request :put "/users/me" data opts))

(defn lookup-avatar
  "Returns the bytes for the user's avatar."
  [user-id & [opts]]
  (http/api-request :get
                    (str "/users/" user-id "/avatar")
                    (assoc opts
                      :return-format :byte-array
                      :return :envelope)))

;; TODO upload-avatar

(defn lookup-cover
  "Returns the bytes for the user's cover."
  [user-id & [opts]]
  (http/api-request :get
                    (str "/users/" user-id "/cover")
                    (assoc opts
                      :return-format :byte-array
                      :return :envelope)))

;; TODO upload-cover

(defn follow-user
  "Follow the given user."
  [user-id & [opts]]
  (http/api-request :post (str "/users/" user-id "/follow") opts))

(defn unfollow-user
  "Unfollow the given user."
  [user-id & [opts]]
  (http/api-request :delete (str "/users/" user-id "/follow") opts))

(defn mute-user
  "Mute the given user."
  [user-id & [opts]]
  (http/api-request :post (str "/users/" user-id "/mute") opts))

(defn unmute-user
  "Unmute the given user."
  [user-id & [opts]]
  (http/api-request :delete (str "/users/" user-id "/mute") opts))

(defn block-user
  "Block the given user."
  [user-id & [opts]]
  (http/api-request :post (str "/users/" user-id "/block" opts)))

(defn unblock-user
  "Unblock the given user."
  [user-id & [opts]]
  (http/api-request :delete (str "/users/" user-id "/block") opts))

(defn search-users
  "Search for users. Optional :count"
  [search-query & [opts]]
  (http/api-request :get "/users/search" (assoc opts :q search-query)))

(defn lookup-following
  "Lookup the users the given user is following."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/following") opts))

(defn lookup-following-ids
  "Lookup the IDS of the users the given user is following."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/following/ids") opts))

(defn lookup-followers
  "Lookup the users following the given user."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/followers") opts))

(defn lookup-followers-ids
  "Lookup the IDs of the users following the given user."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/followers/ids") opts))

(defn lookup-muted-users
  "Lookup the users muted by the given user."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/muted") opts))

(defn lookup-muted-user-ids
  "Lookup the IDs of the users muted by the given users."
  [user-ids & [opts]]
  (http/api-request :get "/users/muted/ids" (assoc opts :ids (join-ids user-ids))))

(defn lookup-blocked-users
  "Lookup the users blocked by the given user."
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/blocked") opts))

(defn lookup-blocked-user-ids
  "Lookup the IDs of the users blocked by the given users."
  [& [opts]]
  (http/api-request :get "/users/blocked/ids" opts))

(defn lookup-repost-users
  "Lookup the users who have reposted the given post."
  [post-id & [opts]]
  (http/api-request :get (str "/posts/" post-id "/reposters") opts))

(defn lookup-star-users
  "Lookup the users who have starred the given post."
  [post-id & [opts]]
  (http/api-request :get (str "/posts/" post-id "/stars") opts))

(defn create-post
  "Create a new post. The first parameter could be a string or a map.
  If it's a string, it will be sent as the text of the post. If it's a
  map, then it requires the :text field. If the post is a reply, then
  it must also contain the :reply-to field, which is the post ID of
  the parent post. Post annotations can also be provided."
  [data & [opts]]
  (let [data (if (string? data)
               {:text data}
               data)]
    (http/api-request :post "/posts" data opts)))

(defn lookup-post
  "Return the specified post."
  [post-id & [opts]]
  (http/api-request :get (str "/posts/" post-id) opts))

(defn lookup-posts
  ""
  [post-ids & [opts]]
  (http/api-request :get "/posts" (assoc opts :ids (join-ids post-ids))))

(defn lookup-user-posts
  [user-id & [opts]]
  (http/api-request :get (str "/users/" user-id "/posts") opts))

(defn delete-post
  "Delete the specified post."
  [post-id & [opts]]
  (http/api-request :delete (str "/posts/" post-id) opts))

(defn repost-post ;; TODO share-post?
  "Share a post (repost) with your followers."
  [post-id & [opts]]
  (http/api-request :post (str "/posts/" post-id "/repost") opts))

(defn unrepost-post ;; TODO unshare-post?
  ""
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
  "Return the 20 most recent posts for a specific hashtag."
  [hashtag & [opts]]
  (http/api-request :get (str "/posts/tag/" hashtag) opts))

(defn retrieve-replies
  "Retrieve the replies to a post."
  [post-id & [opts]]
  (http/api-request :get (str "/posts/" post-id "/replies") opts))

(defn retrieve-stream
  "Return the 20 most recent posts from the current user and the user's
  they follow"
  [& [opts]]
  (http/api-request :get "/posts/stream" opts))

(defn retrieve-unified-stream
  "Return the 20 most recent posts from the current user's
  personalized stream and mentions stream merged into one stream."
  [& [opts]]
  (http/api-request :get "/posts/stream/unified" opts))

(defn retrieve-global-stream
  "Return the 20 most recent posts from the global stream."
  [& [opts]]
  (http/api-request :get "/posts/stream/global" opts))

(defn report-post
  "Report the specified post."
  [post-id & [opts]]
  (http/api-request :post (str "/posts/" post-id "/report") opts))

(defn lookup-place
  [factual-id & [opts]]
  (http/api-request :get (str "/places/" factual-id) opts))

(defn search-places
  [latitude longitude & [opts]]
  (http/api-request :get "/places/search" (assoc opts
                                            :latitude latitude
                                            :longitude longitude)))

(defn lookup-interactions
  [& [opts]]
  (http/api-request :get "/users/me/interactions" opts))

(defn process-text
  [data & [opts]]
  (http/api-request :post "/text/process" data opts))

(defn config-vars
  "Get variables which define the current behavior of the App.net platform."
  [& [opts]]
  (http/api-request :get "/config" opts))

(defn mark-stream
  [data & [opts]]
  (http/api-request :post "/posts/marker" data opts))

(defn lookup-files
  [& [opts]]
  (http/api-request :get "/users/me/files" opts))

(defn lookup-file-contents
  "Get the contents of specific file"
  [file-id & [opts]]
  (http/api-request :get (str "/files/" file-id "/content")
                    opts))
