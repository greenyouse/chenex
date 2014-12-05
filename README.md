# chenex

chenex is a feature expression library in the same vein as the excellent
[cljx](https://github.com/lynaghk/cljx) project. This library tries to
remain true to the cljx implementation while adding a few new features
(see below). The name comes from a combination of
[chen](http://i1.kym-cdn.com/photos/images/original/000/658/650/820.gif)
(honk honk!) and cljx. 


This project has not been released yet, so it will need to be cloned and
installed manually for now.

## Feature Expressions 

If you're used to cljx, then feature expressions will have a slightly
new syntax:

```clj
#+ [clj] (+ 1 1)
```

The `#+` operator is for adding code to some platform. In the case
above, we added `(+ 1 1)` to Clojure. Negation may also be used with
`#-` to omit the code from some platform. For example:

```clj
#- [clj] (+ 1 1)
```

The `#-` will cause the code to be written everywhere but in
Clojure.  Assuming that Clojure and ClojureScript are the only two
platforms we're targeting, this means the code will only show up in our
ClojureScript code.


Now let's try an example with more platforms to display the union
feature. Imagine that we have a project that targets osx, linux, and
windows computers. We can use unions in feature expressions to build
code for multiple platforms:

```clj
#+ [windows linux] (+ 1 1)
```

This puts the code in windows and linux builds but not in osx. An
equivalent expression with negation would be:

```clj
#- [osx] (+ 1 1)
```

All cross-platform code must go in `.cljx` files.

## REPL Problems

REPL middleware is done but some code will not work when evaluating from
a source file. 

To show display what I'm talking about, pretend you're typing this
feature expression in: `#+ [firefox] (+ 1 1)`.

This is a top level feature expression and only `#+ [firefox]` will be
sent to the REPL. This isn't really a problem with the middleware so
much as with the nrepl server software like `Cider`. They weren't
designed with feature expressions in mind (not their fault just
annoying).  

To get around this limitation, grab the code and place it into the REPL
directly. It's not perfect but it does work. 

Hopefully we'll be able to add support to various nrepl server libraries
in the future.

## project.clj

One extra profile must be added to your `project.clj` in order for
chenex to work correctly:

```clj
:profiles {:default [:base :system :user :provided :dev :plugin.chenex/default]}
```

This is a temporary workaround to inject dependecies for chenex and
hopefully will go away around the time of Leiningen 3.0.0.

The `:builds` option in your `project.clj` looks almost identical to the
`:builds` from cljx. Here is what an example build for Clojure might
look like:

```clj 
:chenex {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules {:filetype "clj"
                           :features #{"clj"}
                           :inner-transforms [my.project/expand-regexes
                                              my.project/inject-fn]
                           :outer-transforms []}}]}
```

The only new item is `:inner-transforms`, which sequentially applies any
functions to the code inside of feature expressions. In this case,
`expand-regexes` and then `inject-fn` would be applied to the source
code for Clojure files.  The `:outer-transforms` will apply functions
sequentially to _every_ s-expression in your code.

See the original cljx documentation for information about the options:
[https://github.com/lynaghk/cljx](https://github.com/lynaghk/cljx)

By default, whenever code is compiled with chenex, Leiningen will print
a compile message. If you're using chenex to write a tool of your own
and would like to disable the compiling message add:

```clj
:chenex {:log false
         ...}
```

## Usage

To run chenex once, use:

```sh
$ lein chenex compile
```

For running chenex with auto (via
[`lein-auto`](https://github.com/weavejester/lein-auto)), do: 

```sh
$ lein auto chenex compile
```

If you're going to do a Clojure/ClojureScript build, I have written a
couple default templates to save having to write one yourself. Just run:

```sh
$ lein chenex build cljx 
or
$ lein chenex build browserific
```

That creates a `chenex-builds.clj` with a basic configuration written
for you that looks like this (showing cljx):

```clj
[{:source-paths ["src"]
  :output-path "target/generated-src"
  :rules {:filetype "clj"
          :features #{"clj"}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "target/generated-src"
  :rules {:filetype "cljs"
          :features #{"cljs"}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["test"]
  :output-path "target/generated-test"
  :rules {:filetype "clj"
          :features #{"clj"}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths  ["test"]
  :output-path "target/generated-test"
  :rules {:filetype "cljs"
          :features #{"cljs"}
          :inner-transforms []
          :outer-transforms []}}]
```

To have leiningen pick up the build, add the following to your
project.clj:

```clj
:chenex {:builds ~(-> "chenex-builds.clj" slurp read-string)}
```

This feature is a little experimental, so if you have suggestions for
the template, let me know.

## Projects Using chenex

* [browserific](https://github.com/greenyouse/browserific)

## Thanks

A huge thanks to [cljx](https://github.com/lynaghk/cljx),
[instaparse](https://github.com/Engelberg/instaparse),
[lein-auto](https://github.com/weavejester/lein-auto), and
[sjacket](https://github.com/cgrand/sjacket), without which this project
would not exist. 
