[{:source-paths ["src"]
  :output-path "target/generated-src/chrome"
  :rules {:filetype "cljs"
          :features #{:chrome}
          :inner-transforms []}}
 {:source-paths ["src"]
  :output-path "target/generated-src/opera"
  :rules {:filetype "cljs"
          :features #{:opera}
          :inner-transforms []}}]
