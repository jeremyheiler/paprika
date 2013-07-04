# Paprika

A Clojure library for the [App.net API](http://developers.app.net).

## Usage

Paprika is available on [Clojars](https://clojars.org/com.literallysoftware/paprika). Add the following dependency to your `project.clj` in order to use it.

```clojure
[com.literallysoftware/paprika "0.0.3"]
```

### Getting Started

An [App.net Developer account](https://join.app.net/signup?plan=developer) or a token from [Dev Lite](http://dev-lite.jonathonduerig.com/) is required for authenticated API requests. If you have a developer account, you can [generate a token](https://account.app.net/developer/apps/) from one of your apps. Some endpoints don't require authentication, such as `lookup-user`, but you'll need a token to get the most use out of this library.

#### Setting up a REPL

Once you have a token, launch a REPL and store your token in a var so it's easy to refer to later.

```clojure
(def token "YOUR_TOKEN")
```

Then require `paprika.core`. Here the namespace is aliased as `adn`.

```clojure
(require '[paprika.core :as adn])
```

The next few sections will walk you through using a few of them to give an idea for how they map to the API. Also, you should wrap each call with `clojure.pprint/pprint` so it's easier to read the output.

### Creating a Post

In order to create a post, you need the text for the post and a token.

```clojure
(adn/create-post {:text "I am posting this from my #clojure repl!"} {:access-token token})
```

Each endpoint function follows a general input and output structure. For `create-post`, the first argument is a map that represents the HTTP body. All endpoints that require an HTTP body will accept a map like this a function argument. In this case, the only required value in the map is the text of the post.

The second argument is an options map. Every endpoint function accepts an options map as its last argument, however it is not required if there are no options. The example above shows that the token is provided via the options map.

If you want to specify that the post is a reply to another post, you add the `:reply-to` key with the parent post ID as its value. This is provided with the text because it is part of the HTTP body.

```clojure
(adn/create-post {:text "This is a reply!" :reply-to "POST_ID"} {:access-token token})
```

The value returned from `create-post` is the Post object for the new post.

### Looking Up a User

In order to lookup a user, you need to provide the user's ID or their username.

```clojure
;; Lookup "literally" by username
(adn/lookup-user "@literally")
;; or by ID
(adn/lookup-user "29711")
```

The string "me" can be used along with a token to lookup the currently authenticated user.

```clojure
(adn/lookup-user "me" {:access-token token})
```

The main difference between `lookup-user` and `create-post` is that the former does not have an HTTP body. Instead you need to identify the user, and this information is provided as the first argument. As shown with teh first examples, the options map is not required when there are no options.

Some options you can provide to `lookup-user` are the [general user parameters](http://developers.app.net/docs/resources/user/#general-parameters). Specifically, these are extra query paramters that allow you to alter the result of the request. For example, the API allows you to not include the HTML version of the user's profile description.

```clojure
(adn/lookup-user "@literally" {:include-html 0})
```

You should notice that `:html` is no longer in map for the `:description`.


Each of those calls will return the [User object](http://developers.app.net/docs/resources/user/) for that user by default. Specifically, it returns the value of the `data` key in the [response envelope](http://developers.app.net/docs/basics/responses/#response-envelope). If you want the entire response envelope returned, then you can specify that by setting the `:return` key to `:envelope` in the options map.

```clojure
;; Return the entire response envelope
(adn/lookup-user "me" {:return :envelope})
;;=> {:meta {...} :data {...}}

;; Return the entire response (for debugging)
(adn/lookup-user "me" {:return :response})
;;=> {:status 200 :headers {...} :body {:meta {...} :data {..}}}
```

The default value for `:return` is `:data`. This option is specific to Paprika and is not part of the App.net API.

## Support

* Search the [issues](/issues), and open one (or a pull request) if you didn't find your answer.
* Message [@literally](https://app.net/literally) or [@jeremyheiler](https://app.net/jeremyheiler) on App.net.
* Join the [Paprika](http://patter-app.net/room.html?channel=17641) Patter room.

## License

Copyright © 2013 Literally Software Inc.

Distributed under the Eclipse Public License, the same as Clojure.
