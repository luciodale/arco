(defproject arco "0.2.2"
  :description "Instant formatter to render time that has passed or is left since/to a certain event"
  :url "https://github.com/luciodale/arco"
  :license {:name "MIT"}
  :source-paths ["src"]
  :profiles {:uberjar {:aot :all}}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]])
