[{:source-paths ["src"]
  :output-path "target/generated-src"
  :rules {:filetype "clj"
          :features #{:clj}
          :inner-transforms []}}
 {:source-paths ["src"]
  :output-path "target/generated-src"
  :rules {:filetype "cljs"
          :features #{:cljs}
          :inner-transforms []}}
 {:source-paths ["test"]
  :output-path "target/generated-test"
  :rules {:filetype "clj"
          :features #{:clj}
          :inner-transforms []}}
 {:source-paths  ["test"]
  :output-path "target/generated-test"
  :rules {:filetype "cljs"
          :features #{:cljs}
          :inner-transforms []}}]
