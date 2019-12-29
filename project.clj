(defproject inst "0.1.4"
  :description "Instant formatter for time since an event occurred"
  :url "https://github.com/luciodale/hint"
  :license {:name "MIT"}
  :source-paths ["src"]
  :profiles {:uberjar {:aot :all}}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]])
