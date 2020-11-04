(defproject arco "0.3.0"
  :description "Instant formatter to render time that has passed or is left since/to a certain event"
  :url "https://github.com/luciodale/arco"
  :license {:name "MIT"}
  :dependencies [[tick "0.4.11-alpha"]]
  :source-paths ["src"]
  :profiles {:uberjar {:aot :all}}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]])
