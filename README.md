# chenex

chenex is a feature expression library in the same vein as the excellent
[cljx](https://github.com/lynaghk/cljx) project. This library tries to
remain true to the cljx implementation while adding a few new features
(see below). The name comes from a combination of
[chen](http://i1.kym-cdn.com/photos/images/original/000/658/650/820.gif)
(honk honk!) and cljx. 


## Feature Expressions 

If you're used to cljx, then feature expressions will have a slightly
new syntax:

```clj
(chenex/include! [:clj]  (+ 1 1))
```

The `chenex/include!` function is for adding code to some platform. In
the case above, we added `(+ 1 1)` to Clojure. Negation may also be used
with `chenex/ex!` to omit the code from some platform. For example:

```clj
(chenex/ex! [:clj] (+ 1 1))
```

The `chenex/ex!` will cause the code to be written everywhere but in
Clojure.  Assuming that Clojure and ClojureScript are the only two
platforms we're targeting, this means the code will only show up in our
ClojureScript code.


Now let's try an example with more platforms to display the include
feature. Imagine that we have a project that targets osx, linux, and
windows computers. We can use includes in feature expressions to build
code for multiple platforms:

```clj
(chenex/include! [:windows :linux] (+ 1 1))
```

This puts the code in windows and linux builds but not in osx. An
equivalent expression with negation would be:

```clj
(chenex/ex! [:osx] (+ 1 1))
```


What if we wanted different code for osx, linux, and windows? For
targeting multiple envs there are in-case! (include case) and ex-case!
(exclude case).

Here is what this may look like with using in-case!:

```clj
(chenex/in-case! [:windows]  (println "I'm in windows") 
                 [:osx]  (println "Shows in osx") 
                 :else  (println "This is linux"))
```

The ex-case! is much weaker and should only be used for excluding some
group of platforms while offering alternative code with an `:else`
clause. I'd suggest sticking to in-case! for most complex expressions. An
ex-case! use looks like this:

```clj
(chenex/ex-case! [:windows]  (println "In osx and linux) 
                 :else  (println "This is in windows"))
```


Like in cljx, all cross-platform code must go in `.cljx` files.

## REPL

The REPL also must have a special file at `builds/chenex-repl.clj`
that contains a set one or more chenex builds. For example:

```clj
#{:ios :m}
```

This can be set dynamically while working by either editing the
`chenex-repl.clj` file by hand or doing this:

```sh
$ lein chenex repl :ios
```

This is an ugly hack to make the REPL work. Any ideas for how to load
the REPL env without this would be greatly appreciated! For now it's
probably easier to just edit the file by hand.


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
code for Clojure expressions.  

The `:outer-transforms` are supposed to apply functions sequentially 
to _every_ s-expression in your code but they're not done quite yet.

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
default templates to save having to write one yourself. Just run:

```sh
$ lein chenex build cljx 
```

That creates a `builds/chenex-builds.clj` with a basic configuration
written for you that looks like this (showing cljx):

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
:chenex {:builds ~(-> "builds/chenex-builds.clj" slurp read-string)}
```

This feature is a little experimental, so if you have suggestions for
the template or want to add a new one, let me know.


For things to work properly, you must require `chenex.macros` as
`chenex`.

```clj
;; cljs
(ns your.project
    (:require-macros [chenex.macros :as chenex])
    
;;clj    
(ns your.project
    (:require [chenex.macros :as chenex])
```

## Projects Using chenex

* [browserific](https://github.com/greenyouse/browserific)

## Thanks

A huge thanks to [cljx](https://github.com/lynaghk/cljx),
[lein-auto](https://github.com/weavejester/lein-auto), and
[sjacket](https://github.com/cgrand/sjacket), without which this project
would not exist. 
