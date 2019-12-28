(defproject inst "0.1.0"
  :description "Date formatter for age of events"
  :url "https://github.com/luciodale/hint"
  :license {:name "MIT"}
  :source-paths ["src"]
  :profiles {:uberjar {:aot :all}}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]])
