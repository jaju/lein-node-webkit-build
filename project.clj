(defproject node-webkit-build "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true

  :dependencies [[clj-http "1.0.0"]
                 [grimradical/clj-semver "0.2.0"]
                 [org.apache.directory.studio/org.apache.commons.io "2.4"]
                 [intervox/clj-progress "0.1.1"]]

  :profiles {:dev {:dependencies [[com.gfredericks/vcr-clj "0.4.0"]]}})
