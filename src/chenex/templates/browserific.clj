[;; browsers
 {:source-paths ["src"]
  :output-path "/intermediate/chrome"
  :rules {:filetype "cljs"
          :features #{:b :chrome}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/firefox"
  :rules {:filetype "cljs"
          :features #{:b :firefox}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/opera"
  :rules {:filetype "cljs"
          :features #{:b :opera}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/safari"
  :rules {:filetype "cljs"
          :features #{:b :safari}
          :inner-transforms []
          :outer-transforms []}}

 ;; desktop
 {:source-paths ["src"]
  :output-path "/intermediate/linux32"
  :rules {:filetype "cljs"
          :features #{:d :linux32}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/linux64"
  :rules {:filetype "cljs"
          :features #{:d :linux64}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/osx32"
  :rules {:filetype "cljs"
          :features #{:d :osx32}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/osx64"
  :rules {:filetype "cljs"
          :features #{:d :osx64}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/windows"
  :rules {:filetype "cljs"
          :features #{:d :windows}
          :inner-transforms []
          :outer-transforms []}}

 ;; mobile
 {:source-paths ["src"]
  :output-path "/intermediate/amazon-fire"
  :rules {:filetype "cljs"
          :features #{:m :amazon-fire}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/android"
  :rules {:filetype "cljs"
          :features #{:m :android}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/blackberry"
  :rules {:filetype "cljs"
          :features #{:m :blackberry}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/firefoxos"
  :rules {:filetype "cljs"
          :features #{:m :firefoxos}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/ios"
  :rules {:filetype "cljs"
          :features #{:m :ios}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/ubuntu"
  :rules {:filetype "cljs"
          :features #{:m :ubuntu}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/wp7"
  :rules {:filetype "cljs"
          :features #{:m :wp7}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/wp8"
  :rules {:filetype "cljs"
          :features #{:m :wp8}
          :inner-transforms []
          :outer-transforms []}}
 {:source-paths ["src"]
  :output-path "/intermediate/tizen"
  :rules {:filetype "cljs"
          :features #{:m :tizen}
          :inner-transforms []
          :outer-transforms []}}]
