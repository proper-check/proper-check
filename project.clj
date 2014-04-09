(defproject proper-checks "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/test.check "0.5.7"]
                 [org.clojure/core.match  "0.2.1"]
                 [matchure "0.10.1"]
                 [spyscope "0.1.4"]
                 [org.clojure/tools.namespace  "0.2.4"]]
  :source-paths ["src/clojure" "dev"]
  :java-source-paths ["src/java"])
