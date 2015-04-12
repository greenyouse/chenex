# chenex

chenex is a feature expression library in the same vein as the excellent
[cljx](https://github.com/lynaghk/cljx) project. This library tries to
remain true to the cljx implementation while adding a few new features
(see below). The name comes from a combination of
[chen](http://i1.kym-cdn.com/photos/images/original/000/658/650/820.gif)
(honk honk!) and cljx. 

[![Clojars Project](http://clojars.org/com.greenyouse/chenex/latest-version.svg)](http://clojars.org/com.greenyouse/chenex)

## Feature Expressions 

If you're used to cljx, then chenex feature expressions will have a
slightly new syntax:

```clj
(chenex/in! [:clj]  (+ 1 1))
```

The `chenex/in!` function is for adding code to some platform. In
the case above, we added `(+ 1 1)` to Clojure. Negation may also be used
with `chenex/ex!` to omit the code from some platform. For example:

```clj
(chenex/ex! [:clj] (+ 1 1))
```

The `chenex/ex!` above will cause the code to be written everywhere but in
Clojure.  Assuming that Clojure and ClojureScript are the only two
platforms we're targeting, this means the code will only show up in 
ClojureScript.


Now let's try an example with more platforms to display the include
feature. Imagine that we have a project that targets osx, linux, and
windows computers. We can use includes in feature expressions to build
code for multiple platforms:

```clj
(chenex/in! [:windows :linux] (+ 1 1))
```

This puts the code in windows and linux builds but not in osx. An
equivalent expression with negation would be:

```clj
(chenex/ex! [:osx] (+ 1 1))
```

What if we wanted different code for osx, linux, and windows? For
targeting multiple envs there is `in-case!` (include case).

Here is what this may look like with using `in-case!`:

```clj
(chenex/in-case! [:windows]  (println "I'm in windows") 
                 [:osx]  (println "Shows in osx") 
                 :else  (println "This is linux"))
```

Like in cljx, all cross-platform code must go in `.cljx` files. Any
non-cljx files will be copied over without any special parsing.

## REPL

The REPL also must have a special file at `builds/chenex-repl.clj`
that contains a set of one or more chenex builds. For example:

```clj
#{:ios :m}
```

This can be set dynamically while working by either editing the
`chenex-repl.clj` file by hand or with:

```sh
$ lein chenex repl ios m
```

## project.clj

One extra profile must be added to your `project.clj` in order for
chenex to work correctly:

```clj
:profiles {:default [:base :system :user :provided :dev :plugin.chenex/default]}
```

(This is a temporary workaround to inject dependecies for chenex and
hopefully will go away around the time of Leiningen 3.0.0.)

The `:builds` option in your `project.clj` looks almost identical to the
`:builds` from cljx. Here is what an example build for Clojure might
look like:

```clj 
:chenex {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules {:filetype "clj"
                           :features #{"clj"}
                           :inner-transforms [my.project/expand-regexes
                                              my.project/inject-fn]}}]}
```

The only new item is `:inner-transforms`, which sequentially applies any
functions to the code inside of feature expressions. In this case,
`expand-regexes` and then `inject-fn` would be applied to the source
code for Clojure expressions. 

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
[lein-auto](https://github.com/weavejester/lein-auto)), do: 

```sh
$ lein auto chenex compile
```

If you're going to do a Clojure/ClojureScript build, I have written a
default template to save some time. Just run:

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
          :inner-transforms []}}
 {:source-paths ["src"]
  :output-path "target/generated-src"
  :rules {:filetype "cljs"
          :features #{"cljs"}
          :inner-transforms []}}
 {:source-paths ["test"]
  :output-path "target/generated-test"
  :rules {:filetype "clj"
          :features #{"clj"}
          :inner-transforms []}}
 {:source-paths  ["test"]
  :output-path "target/generated-test"
  :rules {:filetype "cljs"
          :features #{"cljs"}
          :inner-transforms []}}]
```

To have leiningen pick up the build, add the following to your
project.clj:

```clj
:chenex {:builds ~(-> "builds/chenex-builds.clj" slurp read-string)}
```

This feature is a little experimental, so if you have suggestions for
the template or want to add a new one, let me know.


For things to work properly, you must require `chenex` like this:

```clj
(ns your.project
    (:require [greenyouse.chenex :as chenex])
```

The compiler also strips any metadata that uses the `^:` reader
macro. To get around this use `with-meta`.

## Dependencies 

If you're using chenex to write a library that others projects can
depend on (e.g. a library of UI components), you should be mindful of
one major oddity.  

All files must be either clj or cljs (not cljx). To help make this a
bit easier I added a `package` command that converts all cljx files in
the source-paths to either clj or cljs. Use it like this:

```sh
# for clj files
$ lein chenex package clj   

# for cljs files
$ lein chenex package cljs
```

## Projects Using chenex

* [browserific](https://github.com/greenyouse/browserific)

## Thanks

A huge thanks to [cljx](https://github.com/lynaghk/cljx),
[lein-auto](https://github.com/weavejester/lein-auto), and
[sjacket](https://github.com/cgrand/sjacket), without which this project
would not exist. 
