(ns paprika.core
  (:require [clojure.string :as string]
            [paprika.http :refer [define-endpoint]]))

(comment
(define-resource post
  :body-keys []
  :form-keys [:include-muted
              :include-deleted
              :include-directed-posts
              :include-machine
              :include-starred-by
              :include-reposters
              :include-annotations
              :include-post-annotations
              :include-user-annotations
              :include-html])
)

(define-endpoint create-post
  :post "/posts"
  :body-keys [:text :reply-to])

(define-endpoint retrieve-post
  :get "/posts/:post-id")

(define-endpoint delete-post
  :delete "/posts/:post-id")

(define-endpoint repost
  :post "/posts/:post-id/repost")

(define-endpoint unrepost
  :delete "/posts/:post-id/repost")

(define-endpoint star-post
  :post "/posts/:post-id/star")

(define-endpoint unstar-post
  :delete "/posts/:post-id/star")

(define-endpoint retrieve-posts
  :get "/posts"
  :query-keys [:ids]
  :handlers {:ids (fn [v]
                    (if (string? v)
                      v
                      (string/join "," v)))})

(define-endpoint retrieve-posts-created-by-user
  :get "/users/:user-id/posts")

(define-endpoint retrieve-posts-starred-by-user
  :get "/users/:user-id/stars")

(define-endpoint retrieve-posts-with-mention
  :get "/users/:user-id/mentions")

(define-endpoint retrieve-tagged-posts
  :get "/posts/tag/:hashtag")

(define-endpoint retrieve-replies
  :get "/posts/:post-id/replies")

(define-endpoint retrieve-stream
  :get "/posts/stream")

(define-endpoint retrieve-unified-stream
  :get "/posts/stream/unified")

(define-endpoint retrieve-global-stream
  :get "/posts/stream/gloal")

(define-endpoint report-post
  :post "/posts/:post-id/report")
